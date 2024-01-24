package com.lying.tricksy.model;

import java.util.Set;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelCuboidData;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.util.math.Direction;

public class ModelUtils
{
	/** Adds a dilated cuboid with specified faces to the given part builder */
	public static ModelPartBuilder cuboid(ModelPartBuilder builder, float offsetX, float offsetY, float offsetZ, float sizeX, float sizeY, float sizeZ, Dilation extra, Set<Direction> directions)
	{
		builder.cuboidData.add(new ModelCuboidData(null, builder.textureX, builder.textureY, offsetX, offsetY, offsetZ, sizeX, sizeY, sizeZ, extra, builder.mirror, 1.0f, 1.0f, directions));
		return builder;
	}
}
