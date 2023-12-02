package com.lying.tricksy.screen;

import java.util.UUID;

import com.lying.tricksy.api.entity.ITricksyMob;

import net.minecraft.entity.mob.PathAwareEntity;

public interface ITricksySyncable
{
	public void sync(ITricksyMob<?> tricksyIn, PathAwareEntity mobIn);
	
	public void setUUID(UUID idIn);
}
