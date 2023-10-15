package com.lying.tricksy.entity.ai.node;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import com.lying.tricksy.entity.ai.node.subtype.ConditionInventory;
import com.lying.tricksy.entity.ai.node.subtype.ConditionMisc;
import com.lying.tricksy.entity.ai.node.subtype.ConditionWhiteboard;
import com.lying.tricksy.entity.ai.node.subtype.ISubtypeGroup;
import com.lying.tricksy.init.TFNodeTypes;

import net.minecraft.nbt.NbtCompound;

public class ConditionNode extends TreeNode<ConditionNode>
{
	private static final Set<ISubtypeGroup<ConditionNode>> SUBTYPES = Set.of(new ConditionMisc(), new ConditionWhiteboard(), new ConditionInventory());
	
	public ConditionNode(UUID uuidIn)
	{
		super(TFNodeTypes.CONDITION, uuidIn);
	}
	
	public static ConditionNode fromData(UUID uuid, NbtCompound data)
	{
		return new ConditionNode(uuid);
	}
	
	public final boolean canAddChild() { return false; }
	
	public static Collection<ISubtypeGroup<ConditionNode>> getSubtypeGroups() { return SUBTYPES; }
}
