package com.lying.tricksy.utility;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/** Defines a section of a world within which to perform a behaviour */
public abstract class Region
{
	public abstract NbtCompound writeToNbt(NbtCompound data);
	
	public abstract BlockPos center();
	
	public abstract boolean containsPos(BlockPos pos);
	
	public abstract Text describeValue();
	
	public abstract BlockPos findRandomWithin(Random rand);
	
	public abstract <T extends Entity> List<T> getEntitiesByClass(Class<T> type, World world, Predicate<T> filter);
	
	public List<BlockPos> getBlocks(Block block, World world) { return getBlocks(world, (pos,state) -> state.getBlock() == block); }
	
	public abstract List<BlockPos> getBlocks(World world, BiPredicate<BlockPos,BlockState> filter);
	
	protected abstract Box asBox();
}