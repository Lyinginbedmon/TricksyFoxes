package com.lying.tricksy.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

@Mixin(CrossbowItem.class)
public interface CrossbowItemInvoker
{
	@Invoker("postShoot")
	public static void tricksy$postShoot(World world, LivingEntity entity, ItemStack stack) { throw new AssertionError(); }
	
	@Invoker("getProjectiles")
	public static List<ItemStack> tricksy$getProjectiles(ItemStack stack) { throw new AssertionError(); }
	
	@Invoker("createArrow")
	public static PersistentProjectileEntity tricksy$createArrow(World world, LivingEntity entity, ItemStack crossbow, ItemStack arrow) { throw new AssertionError(); }
	
	@Invoker("getSoundPitches")
	public static float[] tricksy$getSoundPitches(Random random) { throw new AssertionError(); }
	
	@Invoker("getSoundPitch")
	public static float tricksy$getSoundPitch(boolean flag, Random random) { throw new AssertionError(); }
}
