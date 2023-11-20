package com.lying.tricksy.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class BlockPrescientCandle extends Block
{
	private static final VoxelShape SHAPE = Block.createCuboidShape(4, 0, 4, 12, 9, 12);
	
	public BlockPrescientCandle(Settings settings)
	{
		super(settings);
		// TODO Auto-generated constructor stub
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return SHAPE; }
}
