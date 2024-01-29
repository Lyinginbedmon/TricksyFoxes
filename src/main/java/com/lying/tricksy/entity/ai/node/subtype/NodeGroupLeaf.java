package com.lying.tricksy.entity.ai.node.subtype;

import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.init.TFNodeTypes;

public abstract class NodeGroupLeaf extends AbstractNodeGroup<LeafNode>
{
	public NodeGroupLeaf() { super(TFNodeTypes.LEAF); }
}
