package com.lying.tricksy.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class BlockPrescience extends Block
{
	private static final VoxelShape SHAPE = VoxelShapes.union(Block.createCuboidShape(2, 0, 2, 14, 11, 14), Block.createCuboidShape(6, 11, 6, 10, 15, 10), Block.createCuboidShape(5.5, 15.025, 5.5, 10.5, 16.225, 10.5));
	
	public BlockPrescience(Settings settings)
	{
		super(settings);
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return SHAPE; }
}
