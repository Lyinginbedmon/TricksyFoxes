package com.lying.tricksy.component;

import java.util.Collection;

import com.lying.tricksy.entity.ITricksyMob;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;

public interface EnlightenmentPath<T extends MobEntity, N extends PathAwareEntity & ITricksyMob<?>>
{
	@SuppressWarnings("unchecked")
	public default N giveEnlightenment(MobEntity entityIn) { return enlighten((T)entityIn); }
	
	public N enlighten(T entityIn);
	
	public boolean conditionsMet(Collection<Accomplishment> accomplishments);
}
