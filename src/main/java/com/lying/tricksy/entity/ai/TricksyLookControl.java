package com.lying.tricksy.entity.ai;

import com.lying.tricksy.api.entity.ITricksyMob;

import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.mob.MobEntity;

public class TricksyLookControl extends LookControl
{
	private final ITricksyMob<?> tricksy;
	
	public <T extends MobEntity & ITricksyMob<?>> TricksyLookControl(T entity)
	{
		super(entity);
		tricksy = entity;
	}
	
	public void tick()
	{
		if(!tricksy.isTreeSleeping())
			super.tick();
	}
	
	protected boolean shouldStayHorizontal() { return true; }
}
