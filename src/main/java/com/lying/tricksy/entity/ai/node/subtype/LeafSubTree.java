package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.UUID;

import com.lying.tricksy.entity.ai.node.ControlFlowNode;
import com.lying.tricksy.entity.ai.node.DecoratorNode;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.node.handler.NodeTickHandler;
import com.lying.tricksy.entity.ai.node.handler.SubTreeHandler;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.init.TFNodeTypes;

import net.minecraft.util.Identifier;

public class LeafSubTree implements ISubtypeGroup<LeafNode>
{
	public static final Identifier VARIANT_COMBAT = ISubtypeGroup.variant("generic_combat");
	
	public void addActions(Collection<NodeSubType<LeafNode>> set)
	{
		// Idle wandering respecting home position
		add(set, VARIANT_COMBAT, genericCombat());
	}
	
	private static NodeTickHandler<LeafNode> genericCombat()
	{
		return new SubTreeHandler()
		{
			public TreeNode<?> generateSubTree()
			{
				return TFNodeTypes.CONTROL_FLOW.create(UUID.randomUUID(), ControlFlowNode.VARIANT_SELECTOR)
					.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafCombat.VARIANT_ATTACK_TRIDENT))
					.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafCombat.VARIANT_ATTACK_POTION))
					.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafCombat.VARIANT_ATTACK_CROSSBOW))
					.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafCombat.VARIANT_ATTACK_BOW))
					.addChild(TFNodeTypes.CONTROL_FLOW.create(UUID.randomUUID(), ControlFlowNode.VARIANT_REACTIVE)
						.addChild(TFNodeTypes.DECORATOR.create(UUID.randomUUID(), DecoratorNode.VARIANT_FORCE_SUCCESS)
							.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafCombat.VARIANT_ATTACK_MELEE)))
						.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafNode.VARIANT_GOTO)
							.assign(CommonVariables.VAR_POS, LocalWhiteboard.ATTACK_TARGET)));
			}
		};
	}
}
