package com.lying.tricksy.item;

import com.lying.tricksy.reference.Reference;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public interface ISealableItem
{
	public default boolean canBeSealed(ItemStack stack) { return true; }
	
	public static boolean isSealable(ItemStack stack) { return stack.getItem() instanceof ISealableItem && ((ISealableItem)stack.getItem()).canBeSealed(stack); }
	
	public static boolean isSealed(ItemStack stack)
	{
		return stack.getOrCreateNbt().getBoolean("Sealed");
	}
	
	public static void seal(ItemStack stack)
	{
		if(!isSealable(stack))
			return;
		NbtCompound nbt = stack.getOrCreateNbt();
		nbt.putBoolean("Sealed", true);
		stack.setNbt(nbt);
	}
	
	public static Text getSealedName(Text nameIn)
	{
		return Text.translatable("item."+Reference.ModInfo.MOD_ID+".sealed_item", nameIn);
	}
}
