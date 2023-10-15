package com.lying.tricksy.entity.ai.node;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import com.lying.tricksy.entity.ai.node.subtype.ControlFlowMisc;
import com.lying.tricksy.entity.ai.node.subtype.ISubtypeGroup;
import com.lying.tricksy.entity.ai.node.subtype.NodeSubType;
import com.lying.tricksy.init.TFNodeTypes;

import net.minecraft.nbt.NbtCompound;

/**
 * NODE TYPES
 * Control Flow	- Executes child nodes in particular ways
 * 		Selector	- Executes the first node that does not return failure
 * 		Sequential	- Executes each node one after the other until end or one returns failure
 * 		Reactive	- Executes all nodes until any return failure or all return success
 */
public class ControlFlowNode extends TreeNode<ControlFlowNode>
{
	public int index = 0;
	
	public ControlFlowNode(UUID uuidIn)
	{
		super(TFNodeTypes.CONTROL_FLOW, uuidIn);
	}
	
	public static ControlFlowNode fromData(UUID uuidIn, NbtCompound data) { return new ControlFlowNode(uuidIn); }
	
	public static Collection<ISubtypeGroup<ControlFlowNode>> getSubtypeGroups() { return Set.of(new ControlFlowMisc()); }
	
	public static void populateSubTypes(Collection<NodeSubType<ControlFlowNode>> set) { }
}
