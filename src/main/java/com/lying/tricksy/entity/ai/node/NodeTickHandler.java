package com.lying.tricksy.entity.ai.node;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.Whiteboard;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;

import net.minecraft.entity.mob.PathAwareEntity;

@FunctionalInterface
public interface NodeTickHandler<M extends TreeNode<?>>
{
	@NotNull
	public <T extends PathAwareEntity & ITricksyMob> Result doTick(T tricksy, Whiteboard local, Whiteboard global, M parent);
}
