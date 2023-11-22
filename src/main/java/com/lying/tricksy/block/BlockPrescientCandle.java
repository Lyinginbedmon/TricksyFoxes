package com.lying.tricksy.block;

import java.util.UUID;
import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.block.entity.PrescientCandleBlockEntity;
import com.lying.tricksy.init.TFBlockEntities;
import com.lying.tricksy.init.TFBlocks;
import com.lying.tricksy.item.ItemPrescientCandle;
import com.lying.tricksy.network.SyncPrescientCandleScreenPacket;
import com.lying.tricksy.screen.PrescientCandleScreenHandler;
import com.lying.tricksy.utility.CandlePowers;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class BlockPrescientCandle extends BlockWithEntity
{
	public static final BooleanProperty PRAYING = BooleanProperty.of("praying");
	private static final VoxelShape SHAPE = Block.createCuboidShape(4, 0, 4, 12, 9, 12);
	public static final ToIntFunction<BlockState> LIGHT_LEVEL = state -> state.get(PRAYING) ? 12 : 7;
	
	public BlockPrescientCandle(Settings settings)
	{
		super(settings);
		setDefaultState(getDefaultState().with(PRAYING, false));
	}
	
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return SHAPE; }
	
	public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL; }
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		return TFBlockEntities.PRESCIENT_CANDLE.instantiate(pos, state);
	}
	
	@Nullable
	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		return canPlaceAt(getDefaultState(), ctx.getWorld(), ctx.getBlockPos()) ? getDefaultState() : null;
	}
	
	@SuppressWarnings("deprecation")
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighbourState, WorldAccess world, BlockPos pos, BlockPos neighbourPos)
	{
		if(direction == Direction.DOWN && !canPlaceAt(state, world, pos))
			return Blocks.AIR.getDefaultState();
		return super.getStateForNeighborUpdate(state, direction, neighbourState, world, pos, neighbourPos);
	}
	
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos)
	{
		return world.getBlockState(pos.down()).isSideSolidFullSquare(world, pos.down(), Direction.UP);
	}
	
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random)
	{
		double x = (double)pos.getX() + 0.5D;
		double y = (double)pos.getY() + 0.8D;
		double z = (double)pos.getZ() + 0.5D;
		world.addParticle(ParticleTypes.SMOKE, x, y, z, 0D, 0D, 0D);
		world.addParticle(ParticleTypes.FLAME, x, y, z, 0D, 0D, 0D);
	}
	
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack)
	{
		BlockEntity tile = world.getBlockEntity(pos);
		if(tile instanceof PrescientCandleBlockEntity)
		{
			PrescientCandleBlockEntity candle = (PrescientCandleBlockEntity)tile;
			UUID uuid = ItemPrescientCandle.getTricksyID(itemStack);
			candle.setTricksyUUID(uuid);
			if(!world.isClient())
				candle.setPower(CandlePowers.getCandlePowers(world.getServer()).getPowerFor(uuid), false);
		}
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
		return checkType(type, TFBlockEntities.PRESCIENT_CANDLE, world.isClient() ? PrescientCandleBlockEntity::tickClient : PrescientCandleBlockEntity::tickServer);
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(PRAYING);
	}
}