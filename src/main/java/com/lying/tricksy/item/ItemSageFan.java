package com.lying.tricksy.item;

import com.lying.tricksy.utility.TricksyOrders;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class ItemSageFan extends Item
{
	public ItemSageFan(Settings settings)
	{
		super(settings);
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
		ItemStack itemStack = user.getStackInHand(hand);
		if(world.isClient())
			TricksyOrders.setTarget();
		user.setCurrentHand(hand);
		return TypedActionResult.consume(itemStack);
	}
	
	public int getMaxUseTime(ItemStack stack) { return 72000; }
	
	public UseAction getUseAction(ItemStack stack) { return UseAction.SPYGLASS; }
	
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks)
	{
		if(world.isClient())
			TricksyOrders.sendOrder((PlayerEntity)user);
	}
}
