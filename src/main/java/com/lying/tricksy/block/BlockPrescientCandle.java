package com.lying.tricksy.block;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.block.entity.PrescientCandleBlockEntity;
import com.lying.tricksy.init.TFBlockEntities;
import com.lying.tricksy.init.TFBlocks;
import com.lying.tricksy.item.ItemPrescientCandle;
import com.lying.tricksy.network.SyncPrescientCandleScreenPacket;
import com.lying.tricksy.screen.PrescientCandleScreenHandler;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BlockPrescientCandle extends BlockWithEntity
{
	private static final VoxelShape SHAPE = Block.createCuboidShape(4, 0, 4, 12, 9, 12);
	
	public BlockPrescientCandle(Settings settings)
	{
		super(settings);
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return SHAPE; }
	
	public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL; }
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return TFBlockEntities.PRESCIENT_CANDLE.instantiate(pos, state);
	}
	
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack)
	{
		BlockEntity tile = world.getBlockEntity(pos);
		if(tile instanceof PrescientCandleBlockEntity)
			((PrescientCandleBlockEntity)tile).setTricksyUUID(ItemPrescientCandle.getTricksyID(itemStack));
	}
	
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
	{
		if(world.isClient())
			return ActionResult.SUCCESS;
		
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if(blockEntity instanceof PrescientCandleBlockEntity)
		{
			UUID tricksyID = ((PrescientCandleBlockEntity)blockEntity).getTricksyUUID();
			player.openHandledScreen(new SimpleNamedScreenHandlerFactory((id, playerInventory, custom) -> new PrescientCandleScreenHandler(id, tricksyID), TFBlocks.PRESCIENT_CANDLE.getName())).ifPresent(syncId -> SyncPrescientCandleScreenPacket.send(player, tricksyID, syncId));
		}
		return ActionResult.CONSUME;
	}
	
	public boolean hasComparatorOutput(BlockState state) { return true; }
	
	public int getComparatorOutput(BlockState state, World world, BlockPos pos)
	{
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if(blockEntity instanceof PrescientCandleBlockEntity)
			return ((PrescientCandleBlockEntity)blockEntity).getCurrentPower();
		
		return 0;
	}
	
	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type)
	{
		return checkType(type, TFBlockEntities.PRESCIENT_CANDLE, world.isClient() ? null : PrescientCandleBlockEntity::tickServer);
	}
}