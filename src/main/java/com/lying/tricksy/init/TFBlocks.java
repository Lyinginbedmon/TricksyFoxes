package com.lying.tricksy.init;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.lying.tricksy.block.BlockClockworkFriar;
import com.lying.tricksy.block.BlockFoxFire;
import com.lying.tricksy.block.BlockOfuda;
import com.lying.tricksy.block.BlockPrescience;
import com.lying.tricksy.block.BlockPrescientCandle;
import com.lying.tricksy.block.BlockWorkTable;
import com.lying.tricksy.reference.Reference;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.dispenser.BlockPlacementDispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.enums.Instrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.BlockView;

public class TFBlocks
{
	private static final Map<Identifier, Block> BLOCKS = new HashMap<>();
	
	public static final Block PRESCIENCE = register("bottle_prescience", new BlockPrescience(FabricBlockSettings.create().luminance((state) -> 8).strength(0.3f).sounds(BlockSoundGroup.GLASS).nonOpaque().pistonBehavior(PistonBehavior.DESTROY).allowsSpawning(TFBlocks::never).solidBlock(TFBlocks::never).suffocates(TFBlocks::never).blockVision(TFBlocks::never)));
	public static final Block WORK_TABLE = register("work_table", new BlockWorkTable(FabricBlockSettings.create().mapColor(MapColor.SPRUCE_BROWN).instrument(Instrument.BASS).strength(2.5F).pistonBehavior(PistonBehavior.BLOCK).sounds(BlockSoundGroup.WOOD)));
	public static final Block CLOCKWORK_FRIAR = register("clockwork_friar", new BlockClockworkFriar(FabricBlockSettings.create().mapColor(MapColor.SPRUCE_BROWN).instrument(Instrument.BASS).pistonBehavior(PistonBehavior.BLOCK).nonOpaque().strength(2.5F).sounds(BlockSoundGroup.WOOD)));
	public static final Block PRESCIENT_CANDLE = register("prescient_candle", new BlockPrescientCandle(FabricBlockSettings.create().strength(0.1F).sounds(BlockSoundGroup.CANDLE).pistonBehavior(PistonBehavior.DESTROY).luminance(BlockPrescientCandle.LIGHT_LEVEL).nonOpaque().allowsSpawning(TFBlocks::never).solidBlock(TFBlocks::never).suffocates(TFBlocks::never).blockVision(TFBlocks::never)));
	public static final Block FOX_FIRE = register("fox_fire", new BlockFoxFire(FabricBlockSettings.create().ticksRandomly().replaceable().nonOpaque().noCollision().pistonBehavior(PistonBehavior.DESTROY).luminance(15).allowsSpawning(TFBlocks::never).solidBlock(TFBlocks::never).suffocates(TFBlocks::never).blockVision(TFBlocks::never)));
	public static final Block OFUDA = register("ofuda", new BlockOfuda(FabricBlockSettings.create().breakInstantly().sounds(BlockSoundGroup.VINE)));
	
	private static Block register(String nameIn, Block blockIn)
	{
		BLOCKS.put(new Identifier(Reference.ModInfo.MOD_ID, nameIn), blockIn);
		return blockIn;
	}
	
	public static void init()
	{
		for(Entry<Identifier, Block> entry : BLOCKS.entrySet())
			Registry.register(Registries.BLOCK, entry.getKey(), entry.getValue());
		
		DispenserBlock.registerBehavior(TFItems.PRESCIENCE_ITEM, new BlockPlacementDispenserBehavior());
		DispenserBlock.registerBehavior(Items.PAPER, new ItemDispenserBehavior()
				{
					public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack)
					{
						ServerWorld serverWorld = pointer.getWorld();
						if (!serverWorld.isClient())
						{
							Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
							Position position = DispenserBlock.getOutputLocation(pointer);
							BlockPos blockPos = pointer.getPos().offset(direction);
							if(serverWorld.getBlockState(blockPos).getBlock() == TFBlocks.PRESCIENCE)
							{
								stack.decrement(1);
								ItemDispenserBehavior.spawnItem(pointer.getWorld(), new ItemStack(TFItems.NOTE), 6, direction, position);
							}
						}
						return stack;
					}
				});
	}
	
	private static boolean never(BlockState state, BlockView world, BlockPos pos) { return false; }
	private static Boolean never(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) { return false; }
}
