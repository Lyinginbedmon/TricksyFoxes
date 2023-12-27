package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.api.entity.ai.INodeIO;
import com.lying.tricksy.api.entity.ai.INodeIOValue;
import com.lying.tricksy.api.entity.ai.INodeIOValue.StaticValue;
import com.lying.tricksy.api.entity.ai.INodeIOValue.WhiteboardValue;
import com.lying.tricksy.api.entity.ai.INodeTickHandler;
import com.lying.tricksy.entity.ai.BehaviourTree.ActionFlag;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.node.handler.CombatHandler;
import com.lying.tricksy.entity.ai.node.handler.NodeInput;
import com.lying.tricksy.entity.ai.node.handler.SubTreeHandler;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardManager;
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
	
	private static INodeTickHandler<LeafNode> genericCombat()
	{
		return new SubTreeHandler()
		{
			public static final WhiteboardRef TARGET = CombatHandler.TARGET;
			
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.of(ActionFlag.HANDS, ActionFlag.MOVE); }
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(TARGET, NodeInput.makeInput(NodeInput.ofType(TFObjType.ENT, false), new WhiteboardObjEntity()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> TreeNode<?> generateSubTree(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				INodeIOValue target;
				if(parent.inputAssigned(TARGET))
					target = parent.getIO(TARGET);
				else
					target = new WhiteboardValue(LocalWhiteboard.ATTACK_TARGET);
				
				return TFNodeTypes.CONTROL_FLOW.create(UUID.randomUUID(), ControlFlowMisc.VARIANT_SELECTOR)
					.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafCombat.VARIANT_ATTACK_TRIDENT).assignIO(TARGET, target))
					.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafCombat.VARIANT_ATTACK_POTION).assignIO(TARGET, target))
					.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafCombat.VARIANT_ATTACK_CROSSBOW).assignIO(TARGET, target))
					.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafCombat.VARIANT_ATTACK_BOW).assignIO(TARGET, target))
					.addChild(TFNodeTypes.CONTROL_FLOW.create(UUID.randomUUID(), ControlFlowMisc.VARIANT_REACTIVE)
						.addChild(TFNodeTypes.DECORATOR.create(UUID.randomUUID(), DecoratorMisc.VARIANT_FORCE_SUCCESS)
							.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafCombat.VARIANT_ATTACK_MELEE).assignIO(TARGET, target)))
						.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafMisc.VARIANT_GOTO)
							.assignIO(CommonVariables.VAR_POS, target)));
			}
		};
	}
	
	private static INodeTickHandler<LeafNode> goPickUp()
	{
		return goAnd(
				val -> TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafInventory.VARIANT_PICK_UP).assignIO(CommonVariables.TARGET_ENT, val), 
				(int)INodeTickHandler.INTERACT_RANGE / 2, 
				CommonVariables.TARGET_ENT, 
				TFObjType.ENT);
	}
	
	private static INodeTickHandler<LeafNode> goBreak()
	{
		return goAnd(
				val -> TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafInteraction.VARIANT_BREAK_BLOCK).assignIO(CommonVariables.VAR_POS, val), 
				(int)INodeTickHandler.INTERACT_RANGE - 1, 
				CommonVariables.VAR_POS, 
				TFObjType.BLOCK);
	}
	
	private static INodeTickHandler<LeafNode> goAnd(Function<INodeIOValue, TreeNode<?>> action, int distance, WhiteboardRef position, TFObjType<?> type)
	{
		return new SubTreeHandler()
		{
			public final WhiteboardRef TARGET = position;
			
			public EnumSet<ActionFlag> flagsUsed() { return EnumSet.of(ActionFlag.HANDS, ActionFlag.MOVE, ActionFlag.LOOK); }
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(TARGET, NodeInput.makeInput(NodeInput.ofType(type, false)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> TreeNode<?> generateSubTree(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				INodeIOValue target = parent.getIO(TARGET);
				return TFNodeTypes.CONTROL_FLOW.create(UUID.randomUUID(), ControlFlowMisc.VARIANT_SELECTOR)
					.addChild(TFNodeTypes.CONTROL_FLOW.create(UUID.randomUUID(), ControlFlowMisc.VARIANT_REACTIVE)
						.addChild(TFNodeTypes.DECORATOR.create(UUID.randomUUID(), DecoratorMisc.VARIANT_INVERTER)
							.addChild(TFNodeTypes.CONDITION.create(UUID.randomUUID(), ConditionMisc.VARIANT_CLOSER_THAN)
								.assignIO(CommonVariables.VAR_POS_A, target)
								.assignIO(CommonVariables.VAR_DIS, new StaticValue(new WhiteboardObj.Int(Math.max(1, distance))))))
						.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafMisc.VARIANT_GOTO).assignIO(CommonVariables.VAR_POS, target)))
					.addChild(TFNodeTypes.CONTROL_FLOW.create(UUID.randomUUID(), ControlFlowMisc.VARIANT_SEQUENCE)
						.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID(), LeafMisc.VARIANT_STOP))
						.addChild(action.apply(target)));
			}
		};
	}
}
