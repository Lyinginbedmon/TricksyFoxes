package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;

import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.node.handler.NodeTickHandler;
import com.lying.tricksy.reference.Reference;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public interface ISubtypeGroup<T extends TreeNode<?>>
{
	public static Identifier variant(String nameIn) { return new Identifier(Reference.ModInfo.MOD_ID, nameIn); }
	
	public Text displayName();
	
	public Collection<NodeSubType<T>> getSubtypes();
	
	public default void add(Collection<NodeSubType<T>> set, Identifier name, NodeTickHandler<T> handler)
	{
		set.add(new NodeSubType<T>(name, handler));
	}
}
