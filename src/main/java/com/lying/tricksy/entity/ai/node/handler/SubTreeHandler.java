package com.lying.tricksy.entity.ai.node.handler;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.api.entity.ai.INodeTickHandler;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;

import net.minecraft.entity.mob.PathAwareEntity;

public abstract class SubTreeHandler implements INodeTickHandler<LeafNode>
{
	public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
	{
		if(!parent.isRunning() || parent.subTree == null)
			parent.subTree = generateSubTree(tricksy, local, global, parent);
		
		return parent.subTree == null ? Result.FAILURE : parent.subTree.tick(tricksy, local, global);
	}
	
	public <T extends PathAwareEntity & ITricksyMob<?>> void onEnd(T tricksy, LeafNode parent)
	{
		parent.subTree.stop(tricksy);
	}
	
	public abstract <T extends PathAwareEntity & ITricksyMob<?>> TreeNode<?> generateSubTree(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent);
}
