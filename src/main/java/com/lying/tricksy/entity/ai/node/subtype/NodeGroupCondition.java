package com.lying.tricksy.entity.ai.node.subtype;

import com.lying.tricksy.entity.ai.node.ConditionNode;
import com.lying.tricksy.init.TFNodeTypes;

public abstract class NodeGroupCondition extends AbstractNodeGroup<ConditionNode>
{
	public NodeGroupCondition() { super(TFNodeTypes.CONDITION); }
}
