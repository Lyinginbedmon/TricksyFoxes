package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.node.handler.NodeTickHandler;
import com.lying.tricksy.entity.ai.node.handler.SubTreeHandler;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.reference.Reference;

import net.minecraft.util.Identifier;

public class LeafSubTree implements ISubtypeGroup<LeafNode>
{
	public static final Identifier VARIANT_COMBAT = ISubtypeGroup.variant("generic_combat");
	
	public Identifier getRegistryName() { return new Identifier(Reference.ModInfo.MOD_ID, "leaf_subtree"); }
	
	public Collection<NodeSubType<LeafNode>> getSubtypes()
	{
		List<NodeSubType<LeafNode>> set = Lists.newArrayList();
		add(set, VARIANT_COMBAT, genericCombat());
		return set;
	}
	
	private static NodeTickHandler<LeafNode> genericCombat()
	{
		return new SubTreeHandler()
		{
			public TreeNode<?> generateSubTree()
			{
				return TFNodeTypes.CONTROL_FLOW.create(UUID.randomUUID(), ControlFlowMisc.VARIANT_SELECTOR)
					.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafCombat.VARIANT_ATTACK_TRIDENT))
					.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafCombat.VARIANT_ATTACK_POTION))
					.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafCombat.VARIANT_ATTACK_CROSSBOW))
					.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafCombat.VARIANT_ATTACK_BOW))
					.addChild(TFNodeTypes.CONTROL_FLOW.create(UUID.randomUUID(), ControlFlowMisc.VARIANT_REACTIVE)
						.addChild(TFNodeTypes.DECORATOR.create(UUID.randomUUID(), DecoratorMisc.VARIANT_FORCE_SUCCESS)
							.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafCombat.VARIANT_ATTACK_MELEE)))
						.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafMisc.VARIANT_GOTO)
							.assignInputRef(CommonVariables.VAR_POS, LocalWhiteboard.ATTACK_TARGET)));
			}
		};
	}
	
//	private static NodeTickHandler<LeafNode> goPickUp()
//	{	// TODO Implement value passing from parent to subtree
//		return new SubTreeHandler()
//		{
//			public static final WhiteboardRef TARGET = CommonVariables.TARGET_ENT;
//			
//			public Map<WhiteboardRef, INodeInput> variableSet()
//			{
//				return Map.of(TARGET, INodeInput.makeInput(INodeInput.ofType(TFObjType.ENT, false)));
//			}
//			
//			public TreeNode<?> generateSubTree()
//			{
//				return TFNodeTypes.CONTROL_FLOW.create(UUID.randomUUID(), ControlFlowMisc.VARIANT_SELECTOR)
//					.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafCombat.VARIANT_ATTACK_TRIDENT))
//					.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafInventory.VARIANT_PICK_UP).assignRef(TARGET, TARGET));
//			}
//		};
//	}
}
