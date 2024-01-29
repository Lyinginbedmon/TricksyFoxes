package com.lying.tricksy.entity.ai.node.subtype;

import com.lying.tricksy.entity.ai.node.ControlFlowNode;
import com.lying.tricksy.init.TFNodeTypes;

public abstract class NodeGroupControlFlow extends AbstractNodeGroup<ControlFlowNode>
{
	public NodeGroupControlFlow() { super(TFNodeTypes.CONTROL_FLOW); }
}
