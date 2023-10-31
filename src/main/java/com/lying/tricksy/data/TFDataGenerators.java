package com.lying.tricksy.data;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class TFDataGenerators implements DataGeneratorEntrypoint
{
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator)
	{
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(TFBlockLootTables::new);
		pack.addProvider(TFRecipeProvider::new);
		pack.addProvider(TFEntityTags::new);
		pack.addProvider(TFBlockTags::new);
		pack.addProvider(TFItemTags::new);
	}
}
