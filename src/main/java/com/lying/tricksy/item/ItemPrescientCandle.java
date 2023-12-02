package com.lying.tricksy.item;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.init.TFBlocks;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class ItemPrescientCandle extends BlockItem
{
	public static final String CANDLE_ID_KEY = "Tricksy";
	
	public ItemPrescientCandle(Settings settings)
	{
		super(TFBlocks.PRESCIENT_CANDLE, settings);
	}
	
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand)
	{
		if(entity instanceof ITricksyMob)
		{
			if(!user.getWorld().isClient())
			{
				UUID entID = entity.getUuid();
				NbtCompound data = stack.getOrCreateNbt();
				data.putUuid(CANDLE_ID_KEY, entID);
				stack.setNbt(data);
				user.setStackInHand(hand, stack);
			}
			return ActionResult.SUCCESS;
		}
		return super.useOnEntity(stack, user, entity, hand);
	}
	
	@Nullable
	public static UUID getTricksyID(ItemStack stack)
	{
		NbtCompound data = stack.getOrCreateNbt();
		if(data.contains(CANDLE_ID_KEY, NbtElement.INT_ARRAY_TYPE))
			return data.getUuid(CANDLE_ID_KEY);
		return null;
	}
}
