package com.lying.tricksy.block.entity;

import java.util.UUID;

import com.lying.tricksy.block.BlockPrescientCandle;
import com.lying.tricksy.init.TFBlockEntities;
import com.lying.tricksy.item.ItemPrescientCandle;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.utility.CandlePowers;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class PrescientCandleBlockEntity extends BlockEntity
{
	public static final BooleanProperty PRAYING = BlockPrescientCandle.PRAYING;
	public static final int PRAYING_TIME = Reference.Values.TICKS_PER_SECOND * 2;
	private UUID tricksyID = null;
	private int currentPower = 0;
	
	private int ticksPraying = 0;
	
	public PrescientCandleBlockEntity(BlockPos pos, BlockState state)
	{
		super(TFBlockEntities.PRESCIENT_CANDLE, pos, state);
	}
	
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		if(nbt.contains(ItemPrescientCandle.CANDLE_ID_KEY, NbtElement.INT_ARRAY_TYPE))
			this.tricksyID = nbt.getUuid(ItemPrescientCandle.CANDLE_ID_KEY);
		else
			this.tricksyID = null;
		this.currentPower = nbt.getInt("Power");
		this.ticksPraying = nbt.getInt("Ticks");
	}
	
	protected void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		if(this.tricksyID != null)
			nbt.putUuid(ItemPrescientCandle.CANDLE_ID_KEY, tricksyID);
		nbt.putInt("Power", currentPower);
		nbt.putInt("Ticks", ticksPraying);
	}
	
	public UUID getTricksyUUID() { return this.tricksyID; }
	
	public void setTricksyUUID(UUID idIn)
	{
		this.tricksyID = idIn;
		this.markDirty();
	}
	
	public int getCurrentPower()
	{
		if(this.tricksyID == null || getWorld().isClient())
			return 0;
		
		return this.currentPower;
	}
	
	public void setPower(int powerIn, boolean notify)
	{
		currentPower = powerIn;
		if(notify)
			ticksPraying = PRAYING_TIME;
		markDirty();
	}
	
	public boolean shouldPray() { return tricksyID != null && ticksPraying > 0; }
	
	public static void tickClient(World world, BlockPos pos, BlockState state, PrescientCandleBlockEntity tile)
	{
		Random rand = world.random;
		if(state.get(PRAYING) && rand.nextInt(4) == 0)
		{
			double x = (double)pos.getX() + 0.5D;
			double y = (double)pos.getY() + 0.8D;
			double z = (double)pos.getZ() + 0.5D;
			
			for(int i=4; i>0; i--)
			{
				double xOff = rand.nextDouble() - 0.5D;
				double yOff = rand.nextDouble() - 0.5D;
				double zOff = rand.nextDouble() - 0.5D;
				world.addParticle(ParticleTypes.SMALL_FLAME, x + xOff, y + yOff, z + zOff, 0D, 0D, 0D);
			}
		}
	}
	
	public static void tickServer(World world, BlockPos pos, BlockState state, PrescientCandleBlockEntity tile)
	{
		int current = tile.getCurrentPower();
		if(tile.tricksyID != null)
		{
			int power = CandlePowers.getCandlePowers(world.getServer()).getPowerFor(tile.tricksyID);
			if(power != current)
				tile.setPower(power, true);
		}
		else if(current > 0)
			tile.setPower(0, false);
		
		boolean shouldPray = tile.shouldPray();
		if(shouldPray != state.get(PRAYING))
			world.setBlockState(pos, state.with(PRAYING, shouldPray), Block.NOTIFY_LISTENERS);
		
		tile.ticksPraying = tile.ticksPraying > 0 ? tile.ticksPraying - 1 : 0;
	}
}
