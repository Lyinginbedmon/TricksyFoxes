package com.lying.tricksy.entity.ai.node;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Global;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Local;

import net.minecraft.entity.mob.PathAwareEntity;

@FunctionalInterface
public interface NodeTickHandler<M extends TreeNode<?>>
{
	@NotNull
	public <T extends PathAwareEntity & ITricksyMob<?>> Result doTick(T tricksy, Local<T> local, Global global, M parent);
}
