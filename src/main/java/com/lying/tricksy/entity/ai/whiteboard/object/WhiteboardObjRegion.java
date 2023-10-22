package com.lying.tricksy.entity.ai.whiteboard.object;

import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.utility.Region;
import com.lying.tricksy.utility.RegionCuboid;
import com.lying.tricksy.utility.RegionSphere;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class WhiteboardObjRegion extends WhiteboardObj<Region, NbtCompound>
{
	public WhiteboardObjRegion()
	{
		super(TFObjType.REGION, NbtElement.COMPOUND_TYPE);
	}
	
	public WhiteboardObjRegion(@NotNull BlockPos posA, @NotNull BlockPos posB)
	{
		this();
		value.clear();
		value.add(RegionCuboid.between(posA, posB));
	}
	
	public Text describeValue(Region value) { return value.describeValue(); }
	
	protected NbtCompound valueToNbt(Region val)
	{
		NbtCompound nbt = new NbtCompound();
		if(val instanceof RegionCuboid)
			nbt.putString("Type", "cube");
		else
			nbt.putString("Type", "sphere");
		return val.writeToNbt(nbt);
	}
	
	protected Region valueFromNbt(NbtCompound nbt)
	{
		Function<NbtCompound, Region> func = RegionCuboid::readFromNbt;
		if(nbt.contains("Type", NbtElement.STRING_TYPE))
		{
			String type = nbt.getString("Type");
			if(type.equalsIgnoreCase("sphere"))
				func = RegionSphere::readFromNbt;
			else if(type.equalsIgnoreCase("cube"))
				func = RegionCuboid::readFromNbt;
		}
		return func.apply(nbt);
	}
}