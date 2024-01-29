package com.lying.tricksy.entity.ai;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.api.entity.ai.INodeIOValue.StaticValue;
import com.lying.tricksy.api.entity.ai.INodeIOValue.WhiteboardValue;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.node.subtype.ConditionMisc;
import com.lying.tricksy.entity.ai.node.subtype.ConditionWhiteboard;
import com.lying.tricksy.entity.ai.node.subtype.ControlFlowMisc;
import com.lying.tricksy.entity.ai.node.subtype.DecoratorMisc;
import com.lying.tricksy.entity.ai.node.subtype.LeafMisc;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.OrderWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.OrderWhiteboard.Order;
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
			ControlFlowMisc.SELECTOR.create().named(Text.translatable("node."+Reference.ModInfo.MOD_ID+".root"))
				.child(DecoratorMisc.DO_ONCE.create()
					.child(LeafMisc.SET_HOME.create(Map.of(CommonVariables.VAR_POS, new WhiteboardValue(LocalWhiteboard.SELF)))))
				.child(ControlFlowMisc.SEQUENCE.create().named(Text.translatable("node."+Reference.ModInfo.MOD_ID+".meander")).discrete()
					.child(ConditionWhiteboard.EQUALS.create(Map.of(
						CommonVariables.VAR_A, new WhiteboardValue(LocalWhiteboard.HAS_SAGE), 
						CommonVariables.VAR_B, new StaticValue(new WhiteboardObj.Bool(false)))))
					.child(LeafMisc.BARK.create(Map.of(CommonVariables.VAR_NUM, new StaticValue(new WhiteboardObj.Int(3)))))
					.child(DecoratorMisc.FORCE_SUCCESS.create()
						.child(LeafMisc.WANDER.create()))
					.child(LeafMisc.LOOK_AROUND.create()))
				.child(ControlFlowMisc.SEQUENCE.create().named(Text.translatable("node."+Reference.ModInfo.MOD_ID+".follow_sage"))
					.child(DecoratorMisc.INVERTER.create()
						.child(ConditionMisc.CLOSER_THAN.create(Map.of(
							CommonVariables.VAR_POS_A, new WhiteboardValue(LocalWhiteboard.NEAREST_SAGE), 
							CommonVariables.VAR_DIS, new StaticValue(new WhiteboardObj.Int(4))))))
					.child(LeafMisc.GOTO.create(Map.of(
						CommonVariables.VAR_POS, new WhiteboardValue(LocalWhiteboard.NEAREST_SAGE)))));
	
	public static final TreeNode<?> COMMAND_DEFAULT =
			ControlFlowMisc.SEQUENCE.create().named(Text.translatable("node."+Reference.ModInfo.MOD_ID+".root"))
				.child(LeafMisc.ORDER_COMPLETE.create());
	
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
	
	public <T extends PathAwareEntity & ITricksyMob<?>> void update(T tricksy, WhiteboardManager<T> whiteboards)
	{
		if(waitTicks > 0)
			--waitTicks;
		
		this.boardCommand.setWorld(tricksy.getWorld());
		whiteboards.add(this.boardCommand);
		
		tricksy.setTreePose(tricksy.defaultPose());
		TreeNode<?> root = (latestTicked = root());
		root.tickLog();
		if(root.tick(tricksy, whiteboards) == Result.FAILURE)
			waitTicks = Reference.Values.TICKS_PER_SECOND;
		
		this.latestLog = root.getLog();
	}
	
	/** Retrieves the status log of the tree most recently ticked */
	public NodeStatusLog latestLog() { return this.latestLog; }
	
	public OrderWhiteboard order() { return this.boardCommand; }
	
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
		if(order().hasOrder())
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
			tree.order().readFromNbt(data.getCompound(COMMAND_KEY));
		
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
