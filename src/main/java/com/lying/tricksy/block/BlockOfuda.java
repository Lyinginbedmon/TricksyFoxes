package com.lying.tricksy.block;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class BlockOfuda extends Block
{
	public static final EnumProperty<Direction> FACING = Properties.FACING;
	protected static final Map<Direction, VoxelShape> SHAPES = Map.of(
			Direction.UP, Block.createCuboidShape(5, 0, 3, 11, 1, 13),
			Direction.DOWN, Block.createCuboidShape(5, 15, 3, 11, 16, 13),
			Direction.NORTH, Block.createCuboidShape(5, 3, 15, 11, 13, 16),
			Direction.SOUTH, Block.createCuboidShape(5, 3, 0, 11, 13, 1),
			Direction.WEST, Block.createCuboidShape(15, 3, 5, 16, 13, 11),
			Direction.EAST, Block.createCuboidShape(0, 3, 5, 1, 13, 11));
	
	public BlockOfuda(Settings settings)
	{
		super(settings);
		this.setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return SHAPES.getOrDefault(state.get(FACING), VoxelShapes.empty());
	}
	
	@SuppressWarnings("deprecation")
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos)
	{
		if(direction == state.get(FACING).getOpposite() && !canPlaceAt(state, world, pos))
			return Blocks.AIR.getDefaultState();
		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}
	
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos)
	{
		Direction direction = state.get(FACING).getOpposite();
		BlockPos blockPos = pos.offset(direction);
		return world.getBlockState(blockPos).isSideSolidFullSquare(world, blockPos, direction.getOpposite());
	}
	
	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		return getDefaultState().with(FACING, ctx.getSide());
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(FACING);
	}
}
