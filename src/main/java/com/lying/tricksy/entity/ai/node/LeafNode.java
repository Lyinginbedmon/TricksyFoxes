package com.lying.tricksy.entity.ai.node;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import com.lying.tricksy.entity.ai.node.subtype.ISubtypeGroup;
import com.lying.tricksy.entity.ai.node.subtype.LeafCombat;
import com.lying.tricksy.entity.ai.node.subtype.LeafGetter;
import com.lying.tricksy.entity.ai.node.subtype.LeafInteraction;
import com.lying.tricksy.entity.ai.node.subtype.LeafInventory;
import com.lying.tricksy.entity.ai.node.subtype.LeafMisc;
import com.lying.tricksy.entity.ai.node.subtype.LeafSearch;
import com.lying.tricksy.entity.ai.node.subtype.LeafSubTree;
import com.lying.tricksy.entity.ai.node.subtype.LeafWhiteboard;
import com.lying.tricksy.init.TFNodeTypes;

import net.minecraft.nbt.NbtCompound;

public class LeafNode extends TreeNode<LeafNode>
{
	public float ticks = 20;
	public TreeNode<?> subTree = null;
	
	private static final Set<ISubtypeGroup<LeafNode>> SUBTYPES = Set.of(
			new LeafMisc(), 
			new LeafWhiteboard(), 
			new LeafInventory(), 
			new LeafInteraction(), 
			new LeafCombat(), 
			new LeafGetter(), 
			new LeafSearch(),
			new LeafSubTree());
	
	public LeafNode(UUID uuidIn)
	{
		super(TFNodeTypes.LEAF, uuidIn);
	}
	
	public static LeafNode fromData(UUID uuid, NbtCompound data)
	{
		return new LeafNode(uuid);
	}
	
	public final boolean canAddChild() { return false; }
	
	public static Collection<ISubtypeGroup<LeafNode>> getSubtypeGroups() { return SUBTYPES; }
}
