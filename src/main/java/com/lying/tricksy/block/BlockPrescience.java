package com.lying.tricksy.block;

import com.lying.tricksy.init.TFItems;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BlockPrescience extends Block implements Waterloggable
{
	private static final VoxelShape SHAPE = VoxelShapes.union(Block.createCuboidShape(2, 0, 2, 14, 11, 14), Block.createCuboidShape(6, 11, 6, 10, 15, 10), Block.createCuboidShape(5.5, 15.025, 5.5, 10.5, 16.225, 10.5));
	
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	
	public BlockPrescience(Settings settings)
	{
		super(settings);
		setDefaultState(getDefaultState().with(WATERLOGGED, false));
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return SHAPE; }
	
	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		BlockPos pos = ctx.getBlockPos();
		World world = ctx.getWorld();
		return super.getPlacementState(ctx).with(WATERLOGGED, world.getFluidState(pos).getFluid() == Fluids.WATER);
	}
	
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
	{
		ItemStack heldStack = player.getStackInHand(hand);
		if(heldStack.getItem() == Items.PAPER)
		{
			if(!world.isClient())
			{
				player.giveItemStack(new ItemStack(TFItems.NOTE));
				if(!player.isCreative())
					heldStack.decrement(1);
			}
			return ActionResult.SUCCESS;
		}
		return ActionResult.PASS;
	}
	
	public FluidState getFluidState(BlockState state)
	{
		return state.get(WATERLOGGED).booleanValue() ? Fluids.WATER.getStill(false) : Fluids.EMPTY.getDefaultState();
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(WATERLOGGED);
	}
}
