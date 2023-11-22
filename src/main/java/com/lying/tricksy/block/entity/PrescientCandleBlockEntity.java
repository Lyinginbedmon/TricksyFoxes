package com.lying.tricksy.block.entity;

import java.util.UUID;

import com.lying.tricksy.init.TFBlockEntities;
import com.lying.tricksy.item.ItemPrescientCandle;
import com.lying.tricksy.utility.CandlePowers;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PrescientCandleBlockEntity extends BlockEntity
{
	private UUID tricksyID = null;
	private int currentPower = 0;
	
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
	}
	
	protected void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		if(this.tricksyID != null)
			nbt.putUuid(ItemPrescientCandle.CANDLE_ID_KEY, tricksyID);
		nbt.putInt("Power", currentPower);
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
	
	private void setPower(int powerIn)
	{
		currentPower = powerIn;
		markDirty();
	}
	
	public static void tickServer(World world, BlockPos pos, BlockState state, PrescientCandleBlockEntity tile)
	{
		int current = tile.currentPower;
		if(tile.tricksyID != null)
		{
			int power = CandlePowers.getCandlePowers(world.getServer()).getPowerFor(tile.tricksyID);
			if(power != current)
				tile.setPower(power);
		}
		else if(current > 0)
			tile.setPower(0);
	}
}
