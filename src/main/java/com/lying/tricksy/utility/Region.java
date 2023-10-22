package com.lying.tricksy.utility;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

/** Defines a section of a world within which to perform a behaviour */
public abstract class Region
{
	public abstract NbtCompound writeToNbt(NbtCompound data);
	
	public abstract BlockPos center();
	
	public abstract boolean containsPos(BlockPos pos);
	
	public abstract Text describeValue();
	
	public abstract BlockPos findRandomWithin(Random rand);
}