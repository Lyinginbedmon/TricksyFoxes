package com.lying.tricksy.item;

import com.lying.tricksy.api.entity.ITricksyMob;

import net.minecraft.item.ItemStack;

public interface IOrderGivingItem
{
	public int getColor(ItemStack stack);
	
	public static boolean isMatchingColor(ITricksyMob<?> mob, ItemStack stack)
	{
		return stack.getItem() instanceof IOrderGivingItem && mob.getColor() == ((IOrderGivingItem)stack.getItem()).getColor(stack);
	}
}
