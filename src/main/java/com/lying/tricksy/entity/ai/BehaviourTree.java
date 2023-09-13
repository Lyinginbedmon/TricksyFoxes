package com.lying.tricksy.entity.ai;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.ConditionNode;
import com.lying.tricksy.entity.ai.node.ControlFlowNode;
import com.lying.tricksy.entity.ai.node.DecoratorNode;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;

/**
 * AI framework used by <@link ITricksyMob> in concert with one or more <@link Whiteboard>
 * @author Lying
 */
public class BehaviourTree
{
	@Nullable
	private final TreeNode<?> root;
	
	public BehaviourTree(@Nullable TreeNode<?> rootIn)
	{
		root = rootIn;
	}
	
	public BehaviourTree()
	{
		TreeNode<?> test = new ControlFlowNode(UUID.randomUUID());
		test.addChild(DecoratorNode.inverter(UUID.randomUUID()).addChild(ConditionNode.isPlayerNearby(UUID.randomUUID(), 10D)));
		test.addChild(ConditionNode.isPlayerNearby(UUID.randomUUID(), 32D));
		test.addChild(new LeafNode(UUID.randomUUID()));
		
		NbtCompound storage = test.write(new NbtCompound());
		root = TreeNode.create(storage);
	}
	
	public <T extends PathAwareEntity & ITricksyMob> void update(T tricksy, Whiteboard local, Whiteboard global)
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
