package com.lying.tricksy.data;

import java.util.concurrent.CompletableFuture;

import com.lying.tricksy.reference.Reference;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.ItemTagProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class TFItemTags extends ItemTagProvider
{
	public static final TagKey<Item> PAPER = TagKey.of(RegistryKeys.ITEM, new Identifier(Reference.ModInfo.MOD_ID, "paper"));
	public static final TagKey<Item> DYE_WHITE = TagKey.of(RegistryKeys.ITEM, new Identifier(Reference.ModInfo.MOD_ID, "dye_white"));
	public static final TagKey<Item> DYE_BLACK = TagKey.of(RegistryKeys.ITEM, new Identifier(Reference.ModInfo.MOD_ID, "dye_black"));
	
	public TFItemTags(FabricDataOutput output, CompletableFuture<WrapperLookup> completableFuture)
	{
		super(output, completableFuture);
	}
	
	protected void configure(WrapperLookup arg)
	{
		getOrCreateTagBuilder(PAPER).add(Items.PAPER);
		getOrCreateTagBuilder(DYE_WHITE).add(Items.WHITE_DYE);
		getOrCreateTagBuilder(DYE_BLACK).add(Items.BLACK_DYE);
	}
}
