package com.lying.tricksy.data;

import com.lying.tricksy.init.TFBlocks;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;

public class TFBlockLootTables extends FabricBlockLootTableProvider
{
	public TFBlockLootTables(FabricDataOutput dataOutput)
	{
		super(dataOutput);
	}
	
	public void generate()
	{
		addDrop(TFBlocks.PRESCIENCE, drops(TFBlocks.PRESCIENCE));
		addDrop(TFBlocks.WORK_TABLE, drops(TFBlocks.WORK_TABLE));
		addDrop(TFBlocks.CLOCKWORK_FRIAR, drops(TFBlocks.CLOCKWORK_FRIAR));
	}
}
