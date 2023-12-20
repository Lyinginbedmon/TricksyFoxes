package com.lying.tricksy.entity;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;

/** State handler for mobs with multiple animations */
public class AnimationManager<T extends Entity>
{
	private final List<AnimationState> states = Lists.newArrayList();
	private int ticksRunning = 0;
	private int currentAnim = -1;
	
	public AnimationManager(int size)
	{
		for(int i=0; i<size; i++)
			states.add(new AnimationState());
	}
	
	public void start(int index, int age)
	{
		for(int i=0; i<states.size(); i++)
		{
			AnimationState state = states.get(i);
			if(i == index)
			{
				state.start(age);
				currentAnim = index;
			}
			else
				state.stop();
		}
		ticksRunning = 0;
	}
	
	public int stopAll()
	{
		states.forEach(state -> state.stop());
		return currentAnim = -1;
	}
	
	public int currentAnim() { return this.currentAnim; }
	
	public int ticksRunning() { return currentAnim < 0 ? 0 : ticksRunning; }
	
	public void tick(T ent)
	{
		if(currentAnim >= 0)
			onUpdateAnim(currentAnim, ++ticksRunning, ent);
	}
	
	/** Called when the manager is ticked, used to provide additional effects like sound events */
	public void onUpdateAnim(int animation, int ticksRunning, T ent) { }
	
	@Nullable
	public AnimationState get(int index)
	{
		return index < 0 || index >= states.size() ? null : states.get(index);
	}
}
