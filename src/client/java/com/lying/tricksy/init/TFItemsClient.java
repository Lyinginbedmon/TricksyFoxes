package com.lying.tricksy.init;

import com.lying.tricksy.item.IOrderGivingItem;
import com.lying.tricksy.item.ISealableItem;
import com.lying.tricksy.reference.Reference;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.item.ClampedModelPredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public class TFItemsClient
{
	public static void registerItemColors()
	{
		ColorProviderRegistry.ITEM.register((stack, index) -> { return index == 0 ? ((DyeableItem)stack.getItem()).getColor(stack) : -1; }, TFItems.SAGE_HAT);
		ColorProviderRegistry.ITEM.register((stack, index) -> { return index == 0 ? ((IOrderGivingItem)stack.getItem()).getColor(stack) : -1; }, TFItems.FAN_COLOR_MAP.values().toArray(new Item[0]));
	}
	
	public static void registerModelPredicates()
	{
		Identifier note_sealed = new Identifier(Reference.ModInfo.MOD_ID, "sealed");
		for(Item scroll : TFItems.SEALABLES)
			ModelPredicateProviderRegistry.register(scroll, note_sealed, (itemStack, clientWorld, livingEntity, seed) -> { return ISealableItem.isSealed(itemStack) ? 1F : 0F; });
		
		Identifier fan_open = new Identifier(Reference.ModInfo.MOD_ID, "open");
		ClampedModelPredicateProvider fan_is_open = (itemStack, clientWorld, livingEntity, seed) -> livingEntity != null && livingEntity.getActiveItem().equals(itemStack) ? 1F : 0F;
		ModelPredicateProviderRegistry.register(TFItems.SAGE_FAN, fan_open, fan_is_open);
		for(Item fan : TFItems.FAN_COLOR_MAP.values())
			ModelPredicateProviderRegistry.register(fan, fan_open, fan_is_open);
	}
}
