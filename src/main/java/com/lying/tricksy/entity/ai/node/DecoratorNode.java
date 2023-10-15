package com.lying.tricksy.entity.ai.node;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import com.lying.tricksy.entity.ai.node.subtype.DecoratorMisc;
import com.lying.tricksy.entity.ai.node.subtype.ISubtypeGroup;
import com.lying.tricksy.entity.ai.node.subtype.NodeSubType;
import com.lying.tricksy.init.TFNodeTypes;

import net.minecraft.nbt.NbtCompound;

/**
 * NODE TYPES
 * Decorator	- Alters the result or modifies the operation of a singular child node
 * 		Force failure	- Always returns failure
 * 		Force success	- Always returns success
 * 		Inverter		- Returns the opposite of the child node (failure = success, success = failure, running unchanged)
 * 		Repeat			- Runs the child N times or until it fails
 * 		Retry			- Runs the child N times or until it succeeds
 * 		Delay			- Runs the child after N ticks
 */
public class DecoratorNode extends TreeNode<DecoratorNode>
{
	
	public int ticks = 20;
	
	protected DecoratorNode(UUID uuidIn)
	{
		super(TFNodeTypes.DECORATOR, uuidIn);
	}
	
	public final boolean canAddChild() { return children().isEmpty(); }
	
	public final boolean isRunnable() { return children().size() == 1; }
	
	public final TreeNode<?> child() { return children().isEmpty() ? null : children().get(0); }
	
	public static DecoratorNode fromData(UUID uuidIn, NbtCompound data)
	{
		return new DecoratorNode(uuidIn);
	}
	
	public static Collection<ISubtypeGroup<DecoratorNode>> getSubtypeGroups() { return Set.of(new DecoratorMisc()); }
	
	public static void populateSubTypes(Collection<NodeSubType<DecoratorNode>> set) { }
}
