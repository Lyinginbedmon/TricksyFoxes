package com.lying.tricksy.utility;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.lying.tricksy.reference.Reference;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class RegionCuboid extends Region
{
	private final BlockPos min, max;
	
	protected RegionCuboid(BlockPos pos1, BlockPos pos2)
	{
		min = pos1;
		max = pos2;
	}
	
	public static RegionCuboid between(BlockPos posA, BlockPos posB)
	{
		/**
		 * Sort the values to ensure one contains all the minimum values and the other all the maximum values
		 * This makes things easier when actually using the values in behaviours
		 */
		int minX = Math.min(posA.getX(), posB.getX());
		int minY = Math.min(posA.getY(), posB.getY());
		int minZ = Math.min(posA.getZ(), posB.getZ());
		
		int maxX = Math.max(posA.getX(), posB.getX());
		int maxY = Math.max(posA.getY(), posB.getY());
		int maxZ = Math.max(posA.getZ(), posB.getZ());
		
		return new RegionCuboid(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
	}
	
	public static RegionCuboid readFromNbt(NbtCompound compound)
	{
		BlockPos min = NbtHelper.toBlockPos(compound.getCompound("Min"));
		BlockPos max = NbtHelper.toBlockPos(compound.getCompound("Max"));
		return RegionCuboid.between(min, max);
	}
	
	public NbtCompound writeToNbt(NbtCompound data)
	{
		data.put("Min", NbtHelper.fromBlockPos(min));
		data.put("Max", NbtHelper.fromBlockPos(max));
		return data;
	}
	
	public Vec3i size() { return new Vec3i(max.getX() - min.getX(), max.getY() - min.getY(), max.getZ() - min.getZ()); }
	
	public Vec3i searchSize() { return new Vec3i(Math.min(256, size().getX()), Math.min(256, size().getY()), Math.min(256, size().getZ())); }
	
	public BlockPos center()
	{
		Vec3i size = size();
		return min.add(size.getX() / 2, 0, size.getZ() / 2);
	}
	
	public boolean containsPos(BlockPos pos)
	{
		if(pos.getX() < min.getX() || pos.getX() > max.getX())
			return false;
		if(pos.getY() < min.getY() || pos.getY() > max.getY())
			return false;
		if(pos.getZ() < min.getZ() || pos.getZ() > max.getZ())
			return false;
		return true;
	}
	
	public Text describeValue() { return Text.translatable("value."+Reference.ModInfo.MOD_ID+".region_cuboid", min.toShortString(), max.toShortString()); }
	
	public BlockPos findRandomWithin(Random rand)
	{
		Vec3i size = size();
		int offX = size.getX() > 0 ? rand.nextInt(size.getX()) : 0;
		int offY = size.getY() > 0 ? rand.nextInt(size.getY()) : 0;
		int offZ = size.getZ() > 0 ? rand.nextInt(size.getZ()) : 0;
		return min.add(offX, offY, offZ);
	}
	
	protected Box asBox()
	{
		return new Box(min.getX(), min.getY(), min.getZ(), max.getX() + 1D, max.getY() + 1D, max.getZ() + 1D);
	}
	
	public <T extends Entity> List<T> getEntitiesByClass(Class<T> type, World world, Predicate<T> filter)
	{
		return world.getEntitiesByClass(type, asBox(), filter);
	}
	
	public List<BlockPos> getBlocks(World world, BiPredicate<BlockPos, BlockState> filter)
	{
		List<BlockPos> matches = Lists.newArrayList();
		Vec3i size = searchSize();
		int sizeX = size.getX() + 1;
		int sizeY = size.getY() + 1;
		int sizeZ = size.getZ() + 1;
		
		for(int y=0; y < sizeY; y++)
		{
			int posY = min.getY() + y;
			if(posY < world.getBottomY())
				continue;
			
			for(int x=0; x < sizeX; x++)
				for(int z=0; z < sizeZ; z++)
				{
					BlockPos offset = new BlockPos(x, 0, z).add(min.getX(), posY, min.getZ());
					if(filter.test(offset, world.getBlockState(offset)))
						matches.add(offset);
					
					if(matches.size() >= 256)
						return matches;
				}
		}
		return matches;
	}
}