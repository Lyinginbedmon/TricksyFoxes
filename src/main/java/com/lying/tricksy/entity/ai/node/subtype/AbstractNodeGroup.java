package com.lying.tricksy.entity.ai.node.subtype;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.api.entity.ai.INodeTickHandler;
import com.lying.tricksy.entity.ai.node.NodeType;
import com.lying.tricksy.entity.ai.node.TreeNode;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.ConstantIntProvider;

public abstract class AbstractNodeGroup<T extends TreeNode<?>> implements ISubtypeGroup<T>
{
	private final NodeType<T> parentType;
	
	protected AbstractNodeGroup(@NotNull NodeType<T> parentIn)
	{
		this.parentType = parentIn;
	}
	
	protected NodeSubType<T> subtype(Identifier registryName, INodeTickHandler<T> func)
	{
		return new NodeSubType<T>(registryName, parentType, func);
	}
	
	protected NodeSubType<T> subtype(Identifier registryName, INodeTickHandler<T> func, int cooldown)
	{
		return new NodeSubType<T>(registryName, parentType, func, ConstantIntProvider.create(cooldown));
	}
}
