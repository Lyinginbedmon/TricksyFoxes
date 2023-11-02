package com.lying.tricksy.block.entity;

import java.util.Optional;

import com.lying.tricksy.block.BlockClockworkFriar;
import com.lying.tricksy.init.TFBlockEntities;
import com.lying.tricksy.reference.Reference;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPointerImpl;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.PositionImpl;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class ClockworkFriarBlockEntity extends BlockEntity
{
	public static final int TIME_TO_CRAFT = Reference.Values.TICKS_PER_SECOND * 1;
	
	private CraftingRecipe nextRecipe = null;
	private int craftTime = 0;
	
	private int ticksCrafting = 0;
	
	public ClockworkFriarBlockEntity(BlockPos pos, BlockState state)
	{
		super(TFBlockEntities.CLOCKWORK_FRIAR, pos, state);
	}
	
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		this.craftTime = nbt.getInt("CraftTicks");
	}
	
	protected void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		nbt.putInt("CraftTicks", craftTime);
	}
	
	public boolean canPlayerUse(PlayerEntity player) { return Inventory.canPlayerUse(this, player); }
	
	protected Text getContainerName() { return Text.translatable("block."+Reference.ModInfo.MOD_ID+".clockwork_friar"); }
	
	public Direction facing()
	{
		Optional<Direction> facing = world.getBlockState(pos).getOrEmpty(BlockClockworkFriar.FACING);
		return facing.isPresent() ? facing.get() : Direction.NORTH;
	}
	
	public void tryCraft()
	{
		if(canCraft() && !isCrafting())
		{
			craftTime = TIME_TO_CRAFT;
			setBlockCrafting(true);
			markDirty();
		}
	}
	
	public void doCraft()
	{
		if(world.isClient())
			return;
		
		ItemStack result = getCraftResult();
		BlockPointerImpl pointer = new BlockPointerImpl((ServerWorld)world, pos);
		Direction direction = world.getBlockState(pos).get(BlockClockworkFriar.FACING);
		
		double d = pointer.getX() + 0.7D * (double)direction.getOffsetX();
		double e = pointer.getY() + 0.7D;
		double f = pointer.getZ() + 0.7D * (double)direction.getOffsetZ();
		Position output = new PositionImpl(d, e, f);
		
		ItemDispenserBehavior.spawnItem(world, result, 6, direction, output);
		pointer.getWorld().syncWorldEvent(WorldEvents.DISPENSER_DISPENSES, pointer.getPos(), 0);
		pointer.getWorld().syncWorldEvent(WorldEvents.DISPENSER_ACTIVATED, pointer.getPos(), direction.getId());
	}
	
	private void setBlockCrafting(boolean crafting)
	{
		if(world.isClient() || world.getBlockState(pos).get(BlockClockworkFriar.CRAFTING) == crafting)
			return;
		world.setBlockState(pos, world.getBlockState(pos).with(BlockClockworkFriar.CRAFTING, crafting), Block.NOTIFY_ALL);
	}
	
	public static void tickClient(World world, BlockPos pos, BlockState state, ClockworkFriarBlockEntity blockEntity)
	{
		if(blockEntity.isCrafting())
		{
			blockEntity.ticksCrafting++;
		}
		else
			blockEntity.ticksCrafting = 0;
	}
	
	public static void tickServer(World world, BlockPos pos, BlockState state, ClockworkFriarBlockEntity blockEntity)
	{
		if(blockEntity.isCrafting())
		{
			if(--blockEntity.craftTime == 0)
			{
				blockEntity.doCraft();
				blockEntity.setBlockCrafting(false);
			}
			blockEntity.markDirty();
		}
	}
	
	public boolean isCrafting()
	{
		return world.isClient() ? world.getBlockState(pos).get(BlockClockworkFriar.CRAFTING) : craftTime > 0;
	}
	
	public int ticksCrafting() { return ticksCrafting; }
	
	public boolean canCraft()
	{
		/**
		 * Check if we have a recipe
		 * Check if neighbouring inventories can supply the recipe
		 */
		return true;
	}
	
	public ItemStack getCraftResult() { return new ItemStack(Items.COAL); }
}
