package com.lying.tricksy.entity.ai;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.node.subtype.ConditionMisc;
import com.lying.tricksy.entity.ai.node.subtype.ConditionWhiteboard;
import com.lying.tricksy.entity.ai.node.subtype.ControlFlowMisc;
import com.lying.tricksy.entity.ai.node.subtype.DecoratorMisc;
import com.lying.tricksy.entity.ai.node.subtype.LeafMisc;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

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
				.addChild(TFNodeTypes.CONDITION.create(UUID.randomUUID(), ConditionWhiteboard.VARIANT_VALUE_EQUALS)
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
	
	private TreeNode<?> root;
	private int waitTicks = 0;
	
	public BehaviourTree() { this(INITIAL_TREE.copy()); }
	
	public BehaviourTree(@Nullable TreeNode<?> rootIn)
	{
		root = rootIn;
	}
	
	public BehaviourTree copy() { return create(storeInNbt()); }
	
	public TreeNode<?> root() { return this.root == null ? (this.root = TFNodeTypes.CONTROL_FLOW.create(UUID.randomUUID())) : this.root; }
	
	public <T extends PathAwareEntity & ITricksyMob<?>> void update(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global)
	{
		if(waitTicks > 0)
			--waitTicks;
		
		tricksy.setSleeping(false);
		TreeNode<?> root = root();
		root.clearLog();
		if(root.tick(tricksy, local, global) == Result.FAILURE)
			waitTicks = Reference.Values.TICKS_PER_SECOND;
	}
	
	public NodeStatusLog latestLog() { return root.getLog(); }
	
	public NbtCompound storeInNbt()
	{
		return root().write(new NbtCompound());
	}
	
	@Nullable
	public static BehaviourTree create(NbtCompound data)
	{
		TreeNode<?> root = TreeNode.create(data);
		return (root == null || root.getType() != TFNodeTypes.CONTROL_FLOW) ? null : new BehaviourTree(root);
	}
	
	/** Returns the total number of nodes in this behaviour tree */
	public int size()
	{
		return recursiveNodeCount(root());
	}
	
	private static int recursiveNodeCount(TreeNode<?> node)
	{
		int tally = 1;
		for(TreeNode<?> child : node.children())
			tally += recursiveNodeCount(child);
		return tally;
	}
	
	public boolean isRunning() { return this.waitTicks == 0; }
}
