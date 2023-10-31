package com.lying.tricksy.block;

import java.util.Random;

import com.lying.tricksy.block.entity.WorkTableBlockEntity;
import com.lying.tricksy.init.TFBlocks;
import com.lying.tricksy.network.SyncWorkTableScreenPacket;
import com.lying.tricksy.screen.WorkTableScreenHandler;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
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
		if(blockEntity instanceof Inventory)
		{
			((Inventory)blockEntity).setStack(10, ItemStack.EMPTY);
			ItemScatterer.spawn(world, pos, (Inventory)((Object)blockEntity));
			world.updateComparators(pos, this);
		}
		
		super.onStateReplaced(state, world, pos, newState, moved);
	}
	
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
	{
		if(world.isClient())
			return ActionResult.SUCCESS;
		
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if(blockEntity instanceof WorkTableBlockEntity)
			player.openHandledScreen(new SimpleNamedScreenHandlerFactory((id, playerInventory, custom) -> new WorkTableScreenHandler(id, playerInventory, ((WorkTableBlockEntity)blockEntity)), TFBlocks.WORK_TABLE.getName())).ifPresent(syncId -> SyncWorkTableScreenPacket.send(player, pos, syncId));
		return ActionResult.CONSUME;
	}
	
	// FIXME Ensure this method actually fires
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random)
	{
		System.out.println("Performing scheduled tick on work table at "+pos.toShortString());
		if(state.get(TRIGGERED))
			((WorkTableBlockEntity)world.getBlockEntity(pos)).tryCraft(true);
	}
	
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify)
	{
		if(world.isClient())
			return;
		
		boolean isPowered = world.isReceivingRedstonePower(pos);
		boolean isTriggered = state.get(TRIGGERED);
		if(isPowered && !isTriggered)
		{
			world.scheduleBlockTick(pos, this, 4);
			((WorkTableBlockEntity)world.getBlockEntity(pos)).tryCraft(true);
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
