package com.lying.tricksy.init;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.lying.tricksy.block.BlockPrescience;
import com.lying.tricksy.reference.Reference;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class TFBlocks
{
	private static final Map<Identifier, Block> BLOCKS = new HashMap<>();
	
	public static final Block PRESCIENCE = register("bottle_prescience", new BlockPrescience(FabricBlockSettings.create().luminance((state) -> 8).strength(0.3f).sounds(BlockSoundGroup.GLASS).nonOpaque().allowsSpawning(TFBlocks::never).solidBlock(TFBlocks::never).suffocates(TFBlocks::never).blockVision(TFBlocks::never)));
	
	private static Block register(String nameIn, Block blockIn)
	{
		BLOCKS.put(new Identifier(Reference.ModInfo.MOD_ID, nameIn), blockIn);
		return blockIn;
	}
	
	public static void init()
	{
		for(Entry<Identifier, Block> entry : BLOCKS.entrySet())
			Registry.register(Registries.BLOCK, entry.getKey(), entry.getValue());
	}
	
	private static boolean never(BlockState state, BlockView world, BlockPos pos) { return false; }
	private static Boolean never(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) { return false; }
}
