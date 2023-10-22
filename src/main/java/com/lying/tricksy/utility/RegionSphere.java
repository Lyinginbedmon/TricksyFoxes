package com.lying.tricksy.utility;

import com.lying.tricksy.reference.Reference;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class RegionSphere extends Region
{
	private final BlockPos center;
	private final int radius;
	
	public RegionSphere(BlockPos pos, int range)
	{
		this.center = pos;
		this.radius = range;
	}
	
	public static RegionSphere readFromNbt(NbtCompound compound)
	{
		BlockPos pos = NbtHelper.toBlockPos(compound.getCompound("Pos"));
		int radius = compound.contains("Radius", NbtElement.INT_TYPE) ? compound.getInt("Radius") : 4;
		return new RegionSphere(pos, radius);
	}
	
	public NbtCompound writeToNbt(NbtCompound data)
	{
		data.put("Pos", NbtHelper.fromBlockPos(center));
		data.putInt("Radius", radius);
		return data;
	}
	
	public BlockPos center() { return this.center; }
	
	public boolean containsPos(BlockPos pos) { return center.isWithinDistance(pos, radius); }
	
	public Text describeValue() { return Text.translatable("value."+Reference.ModInfo.MOD_ID+".region_sphere", center.toShortString(), radius); }
	
	public BlockPos findRandomWithin(Random rand)
	{
		int offX = rand.nextInt(radius * 2) - radius;
		int offY = rand.nextInt(radius) - (radius / 2);
		int offZ = rand.nextInt(radius * 2) - radius;
		return center.add(offX, offY, offZ);
	}
}
