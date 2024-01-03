package com.lying.tricksy.entity.ai;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.node.subtype.ConditionMisc;
import com.lying.tricksy.entity.ai.node.subtype.ConditionWhiteboard;
import com.lying.tricksy.entity.ai.node.subtype.ControlFlowMisc;
import com.lying.tricksy.entity.ai.node.subtype.DecoratorMisc;
import com.lying.tricksy.entity.ai.node.subtype.LeafMisc;
import com.lying.tricksy.entity.ai.whiteboard.OrderWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.OrderWhiteboard.Order;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardManager;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.utility.BehaviourForest;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/**
 * AI framework used by {@link ITricksyMob} in concert with one or more {@link Whiteboard}
 * @author Lying
 */
public class BehaviourTree
{
	/** Default behaviour tree applied on tricksy mob startup before being overridden by NBT */
	public static final TreeNode<?> INITIAL_TREE = 
			TFNodeTypes.CONTROL_FLOW.create(UUID.randomUUID(), ControlFlowMisc.VARIANT_SELECTOR).setCustomName(Text.translatable("node."+Reference.ModInfo.MOD_ID+".root"))
			.addChild(TFNodeTypes.DECORATOR.create(UUID.randomUUID(), DecoratorMisc.VARIANT_DO_ONCE)
				.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafMisc.VARIANT_SET_HOME)
					.assignInputRef(CommonVariables.VAR_POS, LocalWhiteboard.SELF)))
			.addChild(TFNodeTypes.CONTROL_FLOW.create(UUID.randomUUID(), ControlFlowMisc.VARIANT_SEQUENCE).setCustomName(Text.translatable("node."+Reference.ModInfo.MOD_ID+".meander")).setDiscrete(true)
				.addChild(TFNodeTypes.CONDITION.create(UUID.randomUUID(), ConditionWhiteboard.VARIANT_EQUALS)
					.assignInputRef(CommonVariables.VAR_A, LocalWhiteboard.HAS_SAGE)
					.assignInputStatic(CommonVariables.VAR_B, new WhiteboardObj.Bool(false)))
				.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafMisc.VARIANT_BARK)
					.assignInputStatic(CommonVariables.VAR_NUM, new WhiteboardObj.Int(3)))
				.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafMisc.VARIANT_WANDER))
				.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafMisc.VARIANT_LOOK_AROUND)))
			.addChild(TFNodeTypes.CONTROL_FLOW.create(UUID.randomUUID(), ControlFlowMisc.VARIANT_SEQUENCE).setCustomName(Text.translatable("node."+Reference.ModInfo.MOD_ID+".follow_sage"))
				.addChild(TFNodeTypes.DECORATOR.create(UUID.randomUUID(), DecoratorMisc.VARIANT_INVERTER)
					.addChild(TFNodeTypes.CONDITION.create(UUID.randomUUID(), ConditionMisc.VARIANT_CLOSER_THAN)
						.assignInputRef(CommonVariables.VAR_POS_A, LocalWhiteboard.NEAREST_SAGE)
						.assignInputStatic(CommonVariables.VAR_DIS, new WhiteboardObj.Int(4))))
				.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafMisc.VARIANT_GOTO)
					.assignInputRef(CommonVariables.VAR_POS, LocalWhiteboard.NEAREST_SAGE)));
	
	public static final TreeNode<?> COMMAND_DEFAULT =
			TFNodeTypes.CONTROL_FLOW.create(UUID.randomUUID(), ControlFlowMisc.VARIANT_SEQUENCE).setCustomName(Text.translatable("node."+Reference.ModInfo.MOD_ID+".root"))
				.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafMisc.VARIANT_ORDER_COMPLETE));
	
	private static final String TREE_KEY = "Tree";
	private static final String EXECUTOR_KEY = "Executors";
	private static final String COMMAND_KEY = "Command";
	
	/** Primary tree, used when no recognised command is in effect */
	@Nullable
	private TreeNode<?> root;
	
	/** Executor trees, used to execute specific commands */
	private Map<Order, TreeNode<?>> commandNodes = new HashMap<>();
	
	/** Whiteboard storing current command information */
	protected OrderWhiteboard boardCommand = (OrderWhiteboard)(new OrderWhiteboard().build());
	
	private NodeStatusLog latestLog = new NodeStatusLog();
	private TreeNode<?> latestTicked = null;
	private int waitTicks = 0;
	
	public BehaviourTree() { this(INITIAL_TREE.copy()); }
	
	public BehaviourTree(@Nullable TreeNode<?> rootIn)
	{
		root = rootIn;
		for(Order order : Order.values())
			setExecutor(order, COMMAND_DEFAULT.copy());
	}
	
	public BehaviourTree copy() { return create(storeInNbt()); }
	
	/** Root of the current tree being executed */
	public TreeNode<?> root()
	{
		Order command = boardCommand.currentType();
		TreeNode<?> root = root(boardCommand.currentType());
		root.getLog().setTree(commandNodes.containsKey(command) ? command : null);
		return root;
	}
	
	public TreeNode<?> root(Order forOrder)
	{
		return commandNodes.getOrDefault(forOrder, treeRoot());
	}
	
	private TreeNode<?> treeRoot()
	{
		return this.root == null ? (this.root = TFNodeTypes.CONTROL_FLOW.create(UUID.randomUUID())) : this.root;
	}
	
	public <T extends PathAwareEntity & ITricksyMob<?>> void update(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global)
	{
		if(waitTicks > 0)
			--waitTicks;
		
		this.boardCommand.setWorld(tricksy.getWorld());
		
		tricksy.setTreePose(tricksy.defaultPose());
		TreeNode<?> root = (latestTicked = root());
		root.tickLog();
		if(root.tick(tricksy, new WhiteboardManager<T>(local, global, this.boardCommand)) == Result.FAILURE)
			waitTicks = Reference.Values.TICKS_PER_SECOND;
		
		this.latestLog = root.getLog();
	}
	
	/** Retrieves the status log of the tree most recently ticked */
	public NodeStatusLog latestLog() { return this.latestLog; }
	
	public OrderWhiteboard command() { return this.boardCommand; }
	
	public void setExecutor(Order type, TreeNode<?> treeIn) { this.commandNodes.put(type, treeIn); }
	
	public <T extends PathAwareEntity & ITricksyMob<?>> void giveCommand(OrderWhiteboard commandIn, T tricksy)
	{
		this.latestTicked.clearLog();
		this.latestTicked.stop(tricksy);
		this.boardCommand = (OrderWhiteboard)commandIn.copy();
	}
	
	public NbtCompound storeInNbt()
	{
		NbtCompound data = storeTrees(new NbtCompound());
		storeCommand(data);
		return data;
	}
	
	public NbtCompound storeCommand(NbtCompound data)
	{
		if(command().hasOrder())
			data.put(COMMAND_KEY, this.boardCommand.writeToNbt(new NbtCompound()));
		return data;
	}
	
	public NbtCompound storeTrees(NbtCompound data)
	{
		data.put(TREE_KEY, this.treeRoot().write(new NbtCompound()));
		
		NbtList list = new NbtList();
		this.commandNodes.forEach((type,tree) -> 
		{
			NbtCompound nbt = new NbtCompound();
			nbt.putString("Order", type.asString());
			nbt.put("Tree", tree.write(new NbtCompound()));
			list.add(nbt);
		});
		data.put(EXECUTOR_KEY, list);
		
		return data;
	}
	
	@Nullable
	public static BehaviourTree create(NbtCompound data)
	{
		BehaviourTree tree = new BehaviourTree(null);
		tree.setTrees(data);
		if(data.contains(COMMAND_KEY, NbtElement.COMPOUND_TYPE))
			tree.command().readFromNbt(data.getCompound(COMMAND_KEY));
		
		return tree;
	}
	
	public boolean hasTrees() { return this.root != null; }
	
	public void setTrees(NbtCompound data)
	{
		TreeNode<?> root;
		if(data.contains(TREE_KEY, NbtElement.COMPOUND_TYPE))
			root = TreeNode.create(data.getCompound(TREE_KEY));
		else	// Support for older data format, in which only the root node was stored
			root = TreeNode.create(data);
		
		this.root = (root == null || root.getType() != TFNodeTypes.CONTROL_FLOW) ? null : root;
		if(data.contains(EXECUTOR_KEY, NbtElement.LIST_TYPE))
		{
			NbtList list = data.getList(EXECUTOR_KEY, NbtElement.COMPOUND_TYPE);
			for(int i=0; i<list.size(); i++)
			{
				NbtCompound nbt = list.getCompound(i);
				TreeNode<?> node = TreeNode.create(nbt.getCompound("Tree"));
				Order order = Order.fromString(nbt.getString("Order"));
				if(order != null)
					setExecutor(order, node);
			}
		}
	}
	
	/** Returns the total number of nodes in this behaviour tree's primary tree */
	public int size() { return this.root.branchSize(); }
	
	public boolean isRunning() { return this.waitTicks == 0; }
	
	public void logInForest(World world, UUID tricksy)
	{
		if(world.isClient())
			return;
		
		BehaviourForest.getForest(world.getServer()).setTreeFor(tricksy, this);
	}
	
	/** Loads this tree from the forest, or uses the default tree if none is logged */
	public void tryLoadFromForest(World world, UUID tricksy)
	{
		if(world.isClient())
			return;
		
		BehaviourForest forest = BehaviourForest.getForest(world.getServer());
		if(forest.hasTreeFor(tricksy))
			setTrees(forest.getTreeFor(tricksy));
		else
			setTrees(INITIAL_TREE.write(new NbtCompound()));
	}
	
	/** Attempts to synchronise this tree with the one on file in the forest */
	public void syncWithForest(World world, UUID tricksy)
	{
		if(world.isClient())
			return;
		
		if(hasTrees())
			logInForest(world, tricksy);
		else
			tryLoadFromForest(world, tricksy);
	}
	
	public static enum ActionFlag
	{
		LOOK,
		MOVE,
		HANDS;
	}
}
