package com.lying.tricksy.data;

import java.util.function.BiConsumer;

import com.lying.tricksy.init.TFEntityTypes;
import com.lying.tricksy.init.TFItems;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTable.Builder;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.util.Identifier;

public class TFEntityLootTables	extends SimpleFabricLootTableProvider
{
	public TFEntityLootTables(FabricDataOutput output)
	{
		super(output, LootContextTypes.ENTITY);
	}
	
	public void accept(BiConsumer<Identifier, Builder> var1)
	{
		var1.accept(TFEntityTypes.ONRYOJI.getLootTableId(), LootTable.builder()
			.pool(LootPool.builder().rolls(UniformLootNumberProvider.create(1.0f, 3.0f)).with(ItemEntry.builder(Items.PHANTOM_MEMBRANE)))
			.pool(LootPool.builder().rolls(UniformLootNumberProvider.create(0.0f, 5.0f)).with(ItemEntry.builder(TFItems.OFUDA)))
			.pool(LootPool.builder().rolls(UniformLootNumberProvider.create(1.0f, 3.0f)).with(ItemEntry.builder(TFItems.MASTER_TOKEN))));
	}
}
