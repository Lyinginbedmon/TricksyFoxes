package com.lying.tricksy.item;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Rarity;

public class ItemSageHat extends ArmorItem
{
	public ItemSageHat(Settings settings)
	{
		super(ArmorMaterials.LEATHER, Type.HELMET, settings.maxCount(1).fireproof().rarity(Rarity.EPIC));
	}
	
	/** Returns the UUID stored in this hat, or null if there isn't one */
	@Nullable
	public static UUID getMasterID(ItemStack stack)
	{
		NbtCompound stackData = stack.getOrCreateNbt();
		if(stackData.contains("MasterID"))
			return stackData.getUuid("MasterID");
		else
			return null;
	}
	
	/** Returns the UUID stored in this hat, or sets it to that of the living entity if there isn't one */
	@Nullable
	public static UUID getMasterID(ItemStack stack, LivingEntity living)
	{
		NbtCompound stackData = stack.getOrCreateNbt();
		if(stackData.contains("MasterID"))
			return stackData.getUuid("MasterID");
		else if(living != null)
		{
			UUID id = living.getUuid();
			stackData.putUuid("MasterID", id);
			stack.setNbt(stackData);
			return id;
		}
		
		return null;
	}
}
