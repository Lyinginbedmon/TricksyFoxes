package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.INodeValue;
import com.lying.tricksy.entity.ai.node.INodeValue.StaticValue;
import com.lying.tricksy.entity.ai.node.INodeValue.WhiteboardValue;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.node.handler.CombatHandler;
import com.lying.tricksy.entity.ai.node.handler.INodeInput;
import com.lying.tricksy.entity.ai.node.handler.NodeTickHandler;
import com.lying.tricksy.entity.ai.node.handler.SubTreeHandler;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjEntity;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Identifier;

public class LeafSubTree implements ISubtypeGroup<LeafNode>
{
	public static final Identifier VARIANT_COMBAT = ISubtypeGroup.variant("generic_combat");
	public static final Identifier VARIANT_PICKUP = ISubtypeGroup.variant("generic_pickup");
	public static final Identifier VARIANT_BREAK = ISubtypeGroup.variant("generic_break");
	
	public Identifier getRegistryName() { return new Identifier(Reference.ModInfo.MOD_ID, "leaf_subtree"); }
	
	public Collection<NodeSubType<LeafNode>> getSubtypes()
	{
		List<NodeSubType<LeafNode>> set = Lists.newArrayList();
		add(set, VARIANT_COMBAT, genericCombat());
		add(set, VARIANT_PICKUP, goPickUp());
		add(set, VARIANT_BREAK, goBreak());
		return set;
	}
	
	private static NodeTickHandler<LeafNode> genericCombat()
	{
		return new SubTreeHandler()
		{
			public static final WhiteboardRef TARGET = CombatHandler.TARGET;
			
			public Map<WhiteboardRef, INodeInput> inputSet()
			{
				return Map.of(TARGET, INodeInput.makeInput(INodeInput.ofType(TFObjType.ENT, false), new WhiteboardObjEntity()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> TreeNode<?> generateSubTree(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				INodeValue target;
				if(parent.inputAssigned(TARGET))
					target = parent.getInput(TARGET);
				else
					target = new WhiteboardValue(LocalWhiteboard.ATTACK_TARGET);
				
				return TFNodeTypes.CONTROL_FLOW.create(UUID.randomUUID(), ControlFlowMisc.VARIANT_SELECTOR)
					.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafCombat.VARIANT_ATTACK_TRIDENT).assignInput(TARGET, target))
					.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafCombat.VARIANT_ATTACK_POTION).assignInput(TARGET, target))
					.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafCombat.VARIANT_ATTACK_CROSSBOW).assignInput(TARGET, target))
					.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafCombat.VARIANT_ATTACK_BOW).assignInput(TARGET, target))
					.addChild(TFNodeTypes.CONTROL_FLOW.create(UUID.randomUUID(), ControlFlowMisc.VARIANT_REACTIVE)
						.addChild(TFNodeTypes.DECORATOR.create(UUID.randomUUID(), DecoratorMisc.VARIANT_FORCE_SUCCESS)
							.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafCombat.VARIANT_ATTACK_MELEE).assignInput(TARGET, target)))
						.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafMisc.VARIANT_GOTO)
							.assignInput(CommonVariables.VAR_POS, target)));
			}
		};
	}
	
	private static NodeTickHandler<LeafNode> goPickUp()
	{
		return goAnd(
				val -> TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafInventory.VARIANT_PICK_UP).assignInput(CommonVariables.TARGET_ENT, val), 
				(int)NodeTickHandler.INTERACT_RANGE / 2, 
				CommonVariables.TARGET_ENT, 
				TFObjType.ENT);
	}
	
	private static NodeTickHandler<LeafNode> goBreak()
	{
		return goAnd(
				val -> TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafInteraction.VARIANT_BREAK_BLOCK).assignInput(CommonVariables.VAR_POS, val), 
				(int)NodeTickHandler.INTERACT_RANGE - 1, 
				CommonVariables.VAR_POS, 
				TFObjType.BLOCK);
	}
	
	private static NodeTickHandler<LeafNode> goAnd(Function<INodeValue, TreeNode<?>> action, int distance, WhiteboardRef position, TFObjType<?> type)
	{
		return new SubTreeHandler()
		{
			public final WhiteboardRef TARGET = position;
			
			public Map<WhiteboardRef, INodeInput> inputSet()
			{
				return Map.of(TARGET, INodeInput.makeInput(INodeInput.ofType(type, false)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> TreeNode<?> generateSubTree(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				INodeValue target = parent.getInput(TARGET);
				return TFNodeTypes.CONTROL_FLOW.create(UUID.randomUUID(), ControlFlowMisc.VARIANT_SELECTOR)
					.addChild(TFNodeTypes.CONTROL_FLOW.create(UUID.randomUUID(), ControlFlowMisc.VARIANT_REACTIVE)
						.addChild(TFNodeTypes.DECORATOR.create(UUID.randomUUID(), DecoratorMisc.VARIANT_INVERTER)
							.addChild(TFNodeTypes.CONDITION.create(UUID.randomUUID(), ConditionMisc.VARIANT_CLOSER_THAN)
								.assignInput(CommonVariables.VAR_POS_A, target)
								.assignInput(CommonVariables.VAR_DIS, new StaticValue(new WhiteboardObj.Int(Math.max(1, distance))))))
						.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafMisc.VARIANT_GOTO).assignInput(CommonVariables.VAR_POS, target)))
					.addChild(TFNodeTypes.CONTROL_FLOW.create(UUID.randomUUID(), ControlFlowMisc.VARIANT_SEQUENCE)
						.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafMisc.VARIANT_STOP))
						.addChild(action.apply(target)));
			}
		};
	}
}
