package com.lying.tricksy.entity.ai;

import com.lying.tricksy.entity.ITricksyMob;

import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.mob.MobEntity;

public class TricksyLookAroundGoal extends LookAroundGoal
{
	private final ITricksyMob<?> tricksy;
	
	public <T extends MobEntity & ITricksyMob<?>> TricksyLookAroundGoal(T mob)
	{
		super(mob);
		this.tricksy = mob;
	}
	
    public boolean canStart()
    {
        return super.canStart() && !tricksy.getBehaviourTree().isRunning();
    }
    
    public boolean shouldContinue()
    {
        return super.shouldContinue() && !tricksy.getBehaviourTree().isRunning();
    }
}
