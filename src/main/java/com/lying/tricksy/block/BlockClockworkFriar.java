package com.lying.tricksy.block;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.block.entity.ClockworkFriarBlockEntity;
import com.lying.tricksy.init.TFBlockEntities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class BlockClockworkFriar extends BlockWithEntity implements Waterloggable
{
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;
	public static final BooleanProperty CRAFTING = Properties.TRIGGERED;
	
	private static final Map<Direction, VoxelShape> UPPER_SHAPES = Map.of(
			Direction.NORTH, Block.createCuboidShape(0.0, 0.0, 9.0, 16.0, 16.0, 16.0),
			Direction.SOUTH, Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 7.0),
			Direction.EAST, Block.createCuboidShape(0.0, 0.0, 0.0, 7.0, 16.0, 16.0),
			Direction.WEST, Block.createCuboidShape(9.0, 0.0, 0.0, 16.0, 16.0, 16.0));
	
	public BlockClockworkFriar(Settings settings)
	{
		super(settings.pistonBehavior(PistonBehavior.BLOCK));
		setDefaultState(getDefaultState().with(FACING, Direction.NORTH).with(HALF, DoubleBlockHalf.LOWER).with(WATERLOGGED, false).with(CRAFTING, false));
	}
	
	public static boolean isLowerHalf(BlockState state) { return state.get(HALF) == DoubleBlockHalf.LOWER; }
	
	public BlockRenderType getRenderType(BlockState state) { return isLowerHalf(state) ? BlockRenderType.MODEL : BlockRenderType.INVISIBLE; }
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return isLowerHalf(state) ? VoxelShapes.fullCube() : UPPER_SHAPES.getOrDefault(state.get(FACING), VoxelShapes.empty()); }
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return isLowerHalf(state) ? new ClockworkFriarBlockEntity(pos, state) : null;
	}
	
	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		BlockPos pos = ctx.getBlockPos();
		World world = ctx.getWorld();
		if(pos.getY() < world.getTopY() - 1 && world.getBlockState(pos.up()).canReplace(ctx))
			return super.getPlacementState(ctx).with(FACING, ctx.getHorizontalPlayerFacing().getOpposite()).with(WATERLOGGED, world.getFluidState(pos).getFluid() == Fluids.WATER);
		return null;
	}
	
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity place, ItemStack itemStack)
	{
		world.setBlockState(pos.up(), state.with(HALF, DoubleBlockHalf.UPPER).with(WATERLOGGED, world.getFluidState(pos.up()).getFluid() == Fluids.WATER), Block.NOTIFY_ALL);
	}
	
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
	{
		if(world.isClient())
			return ActionResult.SUCCESS;
		
		BlockPos blockPos = isLowerHalf(state) ? pos : pos.down();
		BlockEntity blockEntity = world.getBlockEntity(blockPos);
		if(blockEntity instanceof ClockworkFriarBlockEntity)
		{
			ClockworkFriarBlockEntity friar = (ClockworkFriarBlockEntity)blockEntity;
			if(!friar.isCrafting())
			{
				if(player.isSneaking())
					;	// Open recipe setting window
				else
					friar.tryCraft();
			}
			else
				return ActionResult.PASS;
		}
		return ActionResult.CONSUME;
	}
	
	@SuppressWarnings("deprecation")
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if (state.isOf(newState.getBlock()))
			return;
		
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if(blockEntity instanceof ClockworkFriarBlockEntity)
		{
			((ClockworkFriarBlockEntity)blockEntity).dropHeldStacks();
			world.updateComparators(pos, this);
		}
		
		super.onStateReplaced(state, world, pos, newState, moved);
	}
	
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
	{
		if(world.isClient() || !isLowerHalf(state))
			return;
		
		if(world.isReceivingRedstonePower(pos) && !world.getBlockState(pos).get(CRAFTING))
			((ClockworkFriarBlockEntity)world.getBlockEntity(pos)).tryCraft();
	}
	
	public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player)
	{
		if(!world.isClient())
		{
			BlockPos blockPos = isLowerHalf(state) ? pos.up() : pos.down();
			BlockState blockState = world.getBlockState(blockPos);
			if(blockState.isOf(this))
			{
				BlockState blockState2 = blockState.getFluidState().isOf(Fluids.WATER) ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState();
				world.setBlockState(blockPos, blockState2, Block.NOTIFY_ALL | Block.SKIP_DROPS);
				world.syncWorldEvent(player, WorldEvents.BLOCK_BROKEN, blockPos, Block.getRawIdFromState(blockState));
			}
		}
		super.onBreak(world, pos, state, player);
	}
	
	public FluidState getFluidState(BlockState state)
	{
		return state.get(WATERLOGGED).booleanValue() ? Fluids.WATER.getStill(false) : Fluids.EMPTY.getDefaultState();
	}
	
	public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) { return false; }
	
	public boolean hasComparatorOutput(BlockState state) { return true; }
	
	public int getComparatorOutput(BlockState state, World world, BlockPos pos)
	{
		return state.get(CRAFTING) ? 15 : 0;
	}
	
	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
	{
		return checkType(type, TFBlockEntities.CLOCKWORK_FRIAR, world.isClient() ? ClockworkFriarBlockEntity::tickClient : ClockworkFriarBlockEntity::tickServer);
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(FACING, HALF, WATERLOGGED, CRAFTING);
	}
}
