package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.node.handler.INodeInput;
import com.lying.tricksy.entity.ai.node.handler.NodeTickHandler;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjEntity;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Global;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Local;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public class LeafCombat implements ISubtypeGroup<LeafNode>
{
	public static final Identifier VARIANT_SET_ATTACK = ISubtypeGroup.variant("set_attack");
	public static final Identifier VARIANT_ATTACK_MELEE = ISubtypeGroup.variant("melee_attack");
	
	public void addActions(Collection<NodeSubType<LeafNode>> set)
	{
		set.add(new NodeSubType<LeafNode>(VARIANT_SET_ATTACK, new NodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.TARGET_ENT, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.ENT), new WhiteboardObjEntity()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				IWhiteboardObject<Entity> value = getOrDefault(CommonVariables.TARGET_ENT, parent, local, global).as(TFObjType.ENT);
				if(value.size() == 0)
				{
					tricksy.setTarget(null);
					return Result.SUCCESS;
				}
				
				Entity ent = value.get();
				if(ent instanceof LivingEntity)
				{
					tricksy.setTarget((LivingEntity)ent);
					return Result.SUCCESS;
				}
				else
					return Result.FAILURE;
			}
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_ATTACK_MELEE, new NodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.TARGET_ENT, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.ENT).and((var) -> var != Local.SELF)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				IWhiteboardObject<Entity> value = getOrDefault(CommonVariables.TARGET_ENT, parent, local, global).as(TFObjType.ENT);
				if(value.isEmpty())
					return Result.FAILURE;
				
				Entity ent = value.get();
				if(ent == tricksy || !(ent instanceof LivingEntity) || ent.isSpectator() || ent.getType() == EntityType.PLAYER && ((PlayerEntity)ent).isCreative())
					return Result.FAILURE;
				
				if(!local.canAttack())
					return Result.RUNNING;
				
				LivingEntity living = (LivingEntity)ent;
				if(tricksy.isInAttackRange(living) && !living.isInvulnerable())
				{
					tricksy.swingHand(Hand.MAIN_HAND);
					boolean success = tricksy.tryAttack(living);
					if(success)
						local.setAttackCooldown(Reference.Values.TICKS_PER_SECOND);
					return success ? Result.SUCCESS : Result.FAILURE;
				}
				
				return Result.FAILURE;
			}
		}));
	}
}
