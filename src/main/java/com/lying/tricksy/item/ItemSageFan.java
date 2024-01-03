package com.lying.tricksy.item;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.init.TFSoundEvents;
import com.lying.tricksy.network.OrderStatePacket;
import com.lying.tricksy.utility.TricksyUtils;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class ItemSageFan extends Item implements IOrderGivingItem
{
	@Nullable
	private final DyeColor color;
	
	public ItemSageFan(Settings settings)
	{
		this(null, settings);
	}
	
	public ItemSageFan(DyeColor colorIn, Settings settings)
	{
		super(settings.maxCount(1));
		this.color = colorIn;
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
		ItemStack itemStack = user.getStackInHand(hand);
		if(!world.isClient() && ITricksyMob.isAnySage(user))
			OrderStatePacket.start(user, ((IOrderGivingItem)itemStack.getItem()).getColor(itemStack));
		user.setCurrentHand(hand);
		world.playSound(null, user.getX(), user.getY(), user.getZ(), TFSoundEvents.FAN, SoundCategory.PLAYERS, 0.75f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
		return TypedActionResult.consume(itemStack);
	}
	
	@Override
	public Text getName(ItemStack stack) { return Text.translatable(TFItems.SAGE_FAN.getTranslationKey()); }
	
	public int getColor(ItemStack stack) { return color == null ? -1 : TricksyUtils.componentsToColor(color.getColorComponents()); }
	
	public int getMaxUseTime(ItemStack stack) { return 72000; }
	
	public UseAction getUseAction(ItemStack stack) { return UseAction.TOOT_HORN; }
	
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks)
	{
		world.playSound(null, user.getX(), user.getY(), user.getZ(), TFSoundEvents.FAN, SoundCategory.PLAYERS, 0.75f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.4f));
		if(!world.isClient() && user.getType() == EntityType.PLAYER)
			OrderStatePacket.end((PlayerEntity)user);
	}
}
