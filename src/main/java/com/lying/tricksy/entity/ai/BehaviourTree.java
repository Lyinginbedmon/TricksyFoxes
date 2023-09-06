package com.lying.tricksy.entity.ai;

import com.lying.tricksy.entity.ITricksyMob;

import net.minecraft.entity.LivingEntity;

/**
 * AI framework used by <@link ITricksyMob> in concert with one or more <@link Whiteboard>
 * @author Lying
 */
public class BehaviourTree
{
	public <T extends LivingEntity & ITricksyMob> void update(T tricksy, Whiteboard local, Whiteboard global)
	{
		// TODO Implement tree update procedure
	}
}
