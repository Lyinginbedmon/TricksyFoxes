package com.lying.tricksy.data;

import java.util.function.BiConsumer;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.loot.LootTable.Builder;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.util.Identifier;

public class TFEntityLootTables	extends SimpleFabricLootTableProvider
{
	public TFEntityLootTables(FabricDataOutput output, LootContextType lootContextType)
	{
		super(output, lootContextType);
	}
	
	public void accept(BiConsumer<Identifier, Builder> var1)
	{
		// FIXME Add Onryoji loot table to drop ofuda 0-5, instant master token 1, phantom membrane 1-3
	}
}
