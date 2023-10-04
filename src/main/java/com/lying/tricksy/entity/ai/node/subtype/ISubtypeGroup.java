package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;

import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.reference.Reference;

import net.minecraft.util.Identifier;

public interface ISubtypeGroup<T extends TreeNode<?>>
{
	public static Identifier variant(String nameIn) { return new Identifier(Reference.ModInfo.MOD_ID, nameIn); }
	
	public void addActions(Collection<NodeSubType<T>> set);
}
