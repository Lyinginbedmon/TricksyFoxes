package com.lying.tricksy.entity.ai;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.ConditionNode;
import com.lying.tricksy.entity.ai.node.ControlFlowNode;
import com.lying.tricksy.entity.ai.node.DecoratorNode;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Global;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Local;
import com.lying.tricksy.init.TFNodeTypes;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;

/**
 * AI framework used by <@link ITricksyMob> in concert with one or more <@link Whiteboard>
 * @author Lying
 */
public class BehaviourTree
{
	/** Default behaviour tree applied on tricksy mob startup before being overridden by NBT */
	private static final TreeNode<?> INITIAL_TREE = TFNodeTypes.CONTROL_FLOW.create(UUID.randomUUID(), new NbtCompound()).setSubType(ControlFlowNode.VARIANT_SEQUENCE)
			.addChild(DecoratorNode.inverter(UUID.randomUUID()).addChild(ConditionNode.masterNearby(UUID.randomUUID())))
			.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), new NbtCompound()).setSubType(LeafNode.VARIANT_GOTO));
	
	@Nullable
	private final TreeNode<?> root;
	
	public BehaviourTree() { this(INITIAL_TREE); }
	
	public BehaviourTree(@Nullable TreeNode<?> rootIn)
	{
		root = rootIn;
	}
	
	public <T extends PathAwareEntity & ITricksyMob<?>> void update(T tricksy, Local<T> local, Global global)
	{
		if(root != null)
			root.tick(tricksy, local, global);
	}
	
	public NbtCompound storeInNbt()
	{
		return root.write(new NbtCompound());
	}
	
	public static BehaviourTree create(NbtCompound data)
	{
		TreeNode<?> root = TreeNode.create(data);
		return new BehaviourTree(root);
	}
}
