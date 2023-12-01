package com.lying.tricksy.entity.ai.node.handler;

import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.util.Hand;

public abstract class RangedCombatHandler extends CombatHandler
{
	/** Returns true if the given item is applicable to this attack */
	public abstract boolean isRangeWeapon(ItemStack bowStack);
	
	/** Returns how many ticks should pass between starting and attacking */
	public abstract int getDrawTime();
	
	protected <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result attack(T tricksy, LivingEntity target, LocalWhiteboard<T> local, LeafNode parent)
	{
		ItemStack bowStack = tricksy.getMainHandStack();
		if(!isRangeWeapon(bowStack))
			return Result.FAILURE;
		
		if(!tricksy.isUsingItem())
			tricksy.setCurrentHand(Hand.MAIN_HAND);
		
		if(!parent.isRunning())
			return Result.RUNNING;
		
		if(target.isInvulnerable())
			return Result.FAILURE;
		
		if(tricksy.getItemUseTime() >= getDrawTime())
		{
			attack(target, bowStack, BowItem.getPullProgress(tricksy.getItemUseTime()), tricksy);
			local.setAttackCooldown(Reference.Values.TICKS_PER_SECOND);
			return Result.SUCCESS;
		}
		
		return Result.RUNNING;
	}
	
	protected void attack(LivingEntity target, ItemStack bowStack, float pullProgress, PathAwareEntity shooter)
	{
		attack(target, pullProgress, shooter);
	}
	
	protected abstract void attack(LivingEntity target, float pullProgress, PathAwareEntity shooter);
	
	protected ItemStack getProjectileType(ItemStack stack, PathAwareEntity shooter)
	{
	    if(stack.getItem() instanceof RangedWeaponItem)
	    {
	        Predicate<ItemStack> predicate = ((RangedWeaponItem)stack.getItem()).getHeldProjectiles();
	        ItemStack itemStack = RangedWeaponItem.getHeldProjectile(shooter, predicate);
	        return itemStack.isEmpty() ? new ItemStack(Items.ARROW) : itemStack;
	    }
	    return ItemStack.EMPTY;
	}
	
	public <T extends PathAwareEntity & ITricksyMob<?>> void onEnd(T tricksy, LeafNode parent)
	{
		tricksy.clearActiveItem();
	}
}