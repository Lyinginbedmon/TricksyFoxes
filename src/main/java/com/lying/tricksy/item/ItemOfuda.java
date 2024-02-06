package com.lying.tricksy.item;

import com.lying.tricksy.entity.projectile.EntityOfudaThrown;
import com.lying.tricksy.init.TFEntityTypes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SnowballItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ItemOfuda extends SnowballItem
{
	public ItemOfuda(Settings settings)
	{
		super(settings);
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
		ItemStack itemStack = user.getStackInHand(hand);
		world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
		if(!world.isClient())
		{
			EntityOfudaThrown ofuda = TFEntityTypes.OFUDA_THROWN.create(world);
			ofuda.setPosition(user.getX(), user.getEyeY() - 0.1D, user.getZ());
			ofuda.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 1.5f, 1.0f);
			world.spawnEntity(ofuda);
		}
		user.incrementStat(Stats.USED.getOrCreateStat(this));
		if(!user.getAbilities().creativeMode)
			itemStack.decrement(1);
		return TypedActionResult.success(itemStack, world.isClient());
	}
}
