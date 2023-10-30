package com.lying.tricksy.block;

import java.util.Random;

import com.lying.tricksy.block.entity.WorkTableBlockEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockWorkTable extends BlockWithEntity
{
	public static final BooleanProperty TRIGGERED = Properties.TRIGGERED;
	
	public BlockWorkTable(Settings settings)
	{
		super(settings);
		setDefaultState(this.stateManager.getDefaultState().with(TRIGGERED, false));
	}
	
	public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL; }
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return new WorkTableBlockEntity(pos, state);
	}
	
	@SuppressWarnings("deprecation")
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
	{
		if (state.isOf(newState.getBlock()))
			return;
		
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof Inventory) {
			ItemScatterer.spawn(world, pos, (Inventory)((Object)blockEntity));
			world.updateComparators(pos, this);
		}
		
		super.onStateReplaced(state, world, pos, newState, moved);
	}
	
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random)
	{
		System.out.println("Performing scheduled tick on work table");
		((WorkTableBlockEntity)world.getBlockEntity(pos)).tryCraft();
	}
	
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
	{
		if(world.isClient())
			return;
		
		boolean isPowered = world.isReceivingRedstonePower(pos);
		boolean isTriggered = state.get(TRIGGERED);
		System.out.println("Work table updated: "+isPowered+" - "+isTriggered);
		if(isPowered != isTriggered)
			if(isPowered && !isTriggered)
			{
				System.out.println("Work table triggered");
				world.scheduleBlockTick(pos, this, 4);
				world.setBlockState(pos, state.with(TRIGGERED, true), Block.NO_REDRAW);
			}
			else if(!isPowered && isTriggered)
				world.setBlockState(pos, state.with(TRIGGERED, false), Block.NO_REDRAW);
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(TRIGGERED);
	}
}
