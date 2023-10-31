package com.lying.tricksy.data;

import java.util.concurrent.CompletableFuture;

import com.lying.tricksy.init.TFBlocks;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.BlockTagProvider;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.BlockTags;

public class TFBlockTags extends BlockTagProvider
{
	public TFBlockTags(FabricDataOutput output, CompletableFuture<WrapperLookup> registriesFuture)
	{
		super(output, registriesFuture);
	}
	
	protected void configure(WrapperLookup arg)
	{
		getOrCreateTagBuilder(BlockTags.AXE_MINEABLE).add(TFBlocks.WORK_TABLE);
		getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE).add(TFBlocks.PRESCIENCE);
	}

}
