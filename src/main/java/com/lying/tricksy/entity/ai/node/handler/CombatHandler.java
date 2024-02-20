package com.lying.tricksy.entity.ai.node.handler;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.Predicate;
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.api.entity.ai.INodeIO;
import com.lying.tricksy.api.entity.ai.INodeTickHandler;
import com.lying.tricksy.entity.ai.BehaviourTree.ActionFlag;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardManager;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjEntity;
import com.lying.tricksy.init.TFNodeStatus;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;

public abstract class CombatHandler implements INodeTickHandler<LeafNode>
{
	public static final WhiteboardRef TARGET = CommonVariables.TARGET_ENT;
	public static final Predicate<Entity> VALID_TARGET = (ent) -> 
	{
		if(!(ent instanceof LivingEntity))
			return false;
		else if(!ent.isAlive())
			return false;
		else if(ent.isSpectator())
			return false;
		else if(ent.getType() == EntityType.PLAYER && ((PlayerEntity)ent).isCreative())
			return false;
		return true;
	};
	
	public EnumSet<ActionFlag> flagsUsed() { return EnumSet.of(ActionFlag.HANDS); }
	
	public Map<WhiteboardRef, INodeIO> ioSet()
	{
		Map<WhiteboardRef, INodeIO> set = new HashMap<>();
		set.put(TARGET, NodeInput.makeInput((var) -> var.type() == TFObjType.ENT && !var.isSameRef(LocalWhiteboard.SELF), new WhiteboardObjEntity(), LocalWhiteboard.ATTACK_TARGET.displayName()));
		addVariables(set);
		return set;
	}
	
	protected void addVariables(Map<WhiteboardRef, INodeIO> set) { }
	
	public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent, int tick)
	{
		IWhiteboardObject<Entity> value = getOrDefault(CommonVariables.TARGET_ENT, parent, whiteboards).as(TFObjType.ENT);
		
		Entity ent = null;
		if(!parent.isIOAssigned(TARGET))
			ent = tricksy.getTarget();
		else
			ent = value.get();
		
		if(ent == tricksy || ent == null || !VALID_TARGET.apply(ent))
		{
			parent.logStatus(TFNodeStatus.INPUT_ERROR);
			return Result.FAILURE;
		}
		
		tricksy.lookAtEntity(ent, 10F, tricksy.getMaxLookPitchChange());
		tricksy.setAttacking(true);
		
		// Wait for attack cooldown to finish before starting attack
		
		if(!whiteboards.local().canAttack())
			return Result.RUNNING;
		
		return attack(tricksy, (LivingEntity)ent, whiteboards.local(), parent);
	}
    
    public <T extends PathAwareEntity & ITricksyMob<?>> void onEnd(T tricksy, LeafNode parent)
    {
    	tricksy.setAttacking(false);
    	tricksy.getLocalWhiteboard().setAttackCooldown(Reference.Values.TICKS_PER_SECOND);
    }
	
	protected abstract <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result attack(T tricksy, LivingEntity target, LocalWhiteboard<T> local, LeafNode parent);
}
