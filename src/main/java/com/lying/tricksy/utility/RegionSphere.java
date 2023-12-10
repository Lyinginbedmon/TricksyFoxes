package com.lying.tricksy.utility;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.lying.tricksy.reference.Reference;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

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
	
	protected Box asBox()
	{
		return new Box(center).expand(radius);
	}
	
	public <T extends Entity> List<T> getEntitiesByClass(Class<T> type, World world, Predicate<T> filter)
	{
		Vec3i min = center.add(-radius, -radius, -radius);
		Vec3i max = center.add(radius, radius, radius);
		Box area = new Box(min.getX(), min.getY(), min.getZ(), max.getX() + 1D, max.getY() + 1D, max.getZ() + 1D);
		Predicate<T> rangeFunc = (ent) -> ent.squaredDistanceTo(new Vec3d(center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D)) < radius;
		return world.getEntitiesByClass(type, area, rangeFunc.and(filter));
	}
	
	public List<BlockPos> getBlocks(World world, BiPredicate<BlockPos, BlockState> filter)
	{
		int radius = Math.min(this.radius, 128);
		BiPredicate<BlockPos, BlockState> rangeFunc = (pos,state) -> pos.isWithinDistance(center, radius);
		filter = rangeFunc.and(filter);
		
		List<BlockPos> matches = Lists.newArrayList();
		for(int y=-radius; y < radius; y++)
		{
			int posY = center.getY() + y;
			if(posY < world.getBottomY())
				continue;
			
			for(int x=-radius; x < radius; x++)
				for(int z=-radius; z < radius; z++)
				{
					BlockPos offset = new BlockPos(x, 0, z).add(center.getX(), posY, center.getZ());
					if(filter.test(offset, world.getBlockState(offset)))
						matches.add(offset);
					
					if(matches.size() >= 256)
						return matches;
				}
		}
		return matches;
	}
}
