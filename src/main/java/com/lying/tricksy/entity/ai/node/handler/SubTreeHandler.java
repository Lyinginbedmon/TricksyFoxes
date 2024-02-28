package com.lying.tricksy.entity.ai.node.handler;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.api.entity.ai.INodeTickHandler;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardManager;

import net.minecraft.entity.mob.PathAwareEntity;

public abstract class SubTreeHandler implements INodeTickHandler<LeafNode>
{
	public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onCast(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
	{
		parent.subTree = generateSubTree(tricksy, whiteboards, parent);
		return INodeTickHandler.super.onCast(tricksy, whiteboards, parent);
	}
	
	public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent, int tick)
	{
		return parent.subTree == null ? Result.FAILURE : parent.subTree.tick(tricksy, whiteboards);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends PathAwareEntity & ITricksyMob<?>> void onEnd(T tricksy, LeafNode parent)
	{
		parent.subTree.stop(tricksy, (WhiteboardManager<T>)tricksy.getWhiteboards());
	}
	
	public abstract <T extends PathAwareEntity & ITricksyMob<?>> TreeNode<?> generateSubTree(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent);
}
