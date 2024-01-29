package com.lying.tricksy.entity.ai.node.subtype;

import com.lying.tricksy.entity.ai.node.DecoratorNode;
import com.lying.tricksy.init.TFNodeTypes;

public abstract class NodeGroupDecorator extends AbstractNodeGroup<DecoratorNode>
{
	public NodeGroupDecorator() { super(TFNodeTypes.DECORATOR); }
}
