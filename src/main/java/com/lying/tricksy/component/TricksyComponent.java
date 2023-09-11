package com.lying.tricksy.component;

import java.util.Collection;
import java.util.List;

import com.lying.tricksy.entity.ITricksyMob;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;

/**
 * Capability-esque class that manages tracking of and conversion resulting from Tricksy enlightenment
 * @author Lying
 */
public final class TricksyComponent implements Component
{
	private static final Collection<EntityType<? extends MobEntity>> ENLIGHTENABLES = List.of(EntityType.FOX);
	
	/** The mob this component is monitoring */
	private final MobEntity theMob;
	
	/** True only if the monitored mob can actually become enlightened and isn't already */
	private final boolean canEnlighten;
	
	public TricksyComponent(MobEntity entityIn)
	{
		theMob = entityIn;
		canEnlighten = !(entityIn instanceof ITricksyMob) && ENLIGHTENABLES.contains(entityIn.getType());
	}
	
	public boolean canBeEnlightened() { return this.canEnlighten; }
	
	@Override
	public void readFromNbt(NbtCompound tag)
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void writeToNbt(NbtCompound tag)
	{
		// TODO Auto-generated method stub
		
	}
}
