package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
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
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Identifier;

public class LeafSubTree extends NodeGroupLeaf
{
	public static NodeSubType<LeafNode> COMBAT;
	public static NodeSubType<LeafNode> PICKUP;
	public static NodeSubType<LeafNode> BREAK;
	
	public Identifier getRegistryName() { return new Identifier(Reference.ModInfo.MOD_ID, "leaf_subtree"); }
	
	public Collection<NodeSubType<LeafNode>> getSubtypes()
	{
		List<NodeSubType<LeafNode>> set = Lists.newArrayList();
		set.add(COMBAT = subtype(ISubtypeGroup.variant("generic_combat"), genericCombat()));
		set.add(PICKUP = subtype(ISubtypeGroup.variant("generic_pickup"), goPickUp()));
		set.add(BREAK = subtype(ISubtypeGroup.variant("generic_break"), goBreak()));
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
				if(parent.isIOAssigned(TARGET))
					target = parent.getIO(TARGET);
				else
					target = new WhiteboardValue(LocalWhiteboard.ATTACK_TARGET);
				
				return ControlFlowMisc.SELECTOR.create()
					.child(LeafCombat.ATTACK_TRIDENT.create(Map.of(TARGET, target)))
					.child(LeafCombat.ATTACK_POTION.create(Map.of(TARGET, target)))
					.child(LeafCombat.ATTACK_CROSSBOW.create(Map.of(TARGET, target)))
					.child(LeafCombat.ATTACK_BOW.create(Map.of(TARGET, target)))
					.child(ControlFlowMisc.REACTIVE.create()
						.child(DecoratorMisc.FORCE_SUCCESS.create()
							.child(LeafCombat.ATTACK_MELEE.create(Map.of(TARGET, target))))
						.child(LeafMisc.GOTO.create(Map.of(CommonVariables.VAR_POS, target))));
			}
		};
	}
	
	private static INodeTickHandler<LeafNode> goPickUp()
	{
		return goAnd(
				val -> LeafInventory.PICK_UP.create(Map.of(CommonVariables.TARGET_ENT, val)), 
				(int)INodeTickHandler.INTERACT_RANGE / 2, 
				CommonVariables.TARGET_ENT, 
				TFObjType.ENT);
	}
	
	private static INodeTickHandler<LeafNode> goBreak()
	{
		return goAnd(
				val -> LeafInteraction.BREAK_BLOCK.create(Map.of(CommonVariables.VAR_POS, val)), 
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
				return ControlFlowMisc.SEQUENCE.create()
					.child(DecoratorMisc.FORCE_SUCCESS.create()
						.child(ControlFlowMisc.REACTIVE.create()
							.child(DecoratorMisc.INVERTER.create()
								.child(ConditionMisc.CLOSER_THAN.create(Map.of(
									CommonVariables.VAR_POS_A, target, 
									CommonVariables.VAR_DIS, new StaticValue(new WhiteboardObj.Int(Math.max(1, distance)))))))
							.child(LeafMisc.GOTO.create(Map.of(CommonVariables.VAR_POS, target)))))
					.child(ControlFlowMisc.SEQUENCE.create()
						.child(LeafMisc.STOP.create())
						.child(action.apply(target)));
			}
		};
	}
}
