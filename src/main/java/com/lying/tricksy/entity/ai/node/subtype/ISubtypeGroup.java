package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.api.entity.ai.INodeTickHandler;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public interface ISubtypeGroup<T extends TreeNode<?>>
{
	public static Identifier variant(String nameIn) { return new Identifier(Reference.ModInfo.MOD_ID, nameIn); }
	
	public Identifier getRegistryName();
	
	public default Text displayName() { return Text.translatable("subtype."+getRegistryName().getNamespace()+"."+getRegistryName().getPath()); }
	
	public Collection<NodeSubType<T>> getSubtypes();
	
	public default Collection<NodeSubType<T>> getSubtypesFor(@Nullable EntityType<?> typeIn)
	{
		Collection<NodeSubType<T>> subtypes = getSubtypes();
		if(typeIn != null)
			subtypes.removeIf(subtype -> !subtype.isValidFor(typeIn));
		return subtypes;
	}
	
	public default void add(Collection<NodeSubType<T>> set, Identifier name, INodeTickHandler<T> handler)
	{
		set.add(new NodeSubType<T>(name, handler));
	}
}
