package com.lying.tricksy.component;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.lying.tricksy.api.entity.ITricksyMob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Identifier;

public interface EnlightenmentPath<T extends MobEntity, N extends PathAwareEntity & ITricksyMob<?>>
{
	public EntityType<N> resultType();
	
	@SuppressWarnings("unchecked")
	public default N giveEnlightenment(MobEntity entityIn) { return enlighten((T)entityIn); }
	
	public N enlighten(T entityIn);
	
	public boolean conditionsMet(Collection<Accomplishment> accomplishments);
	
	public default boolean hasReachedMastery(Collection<Accomplishment> accomplishments) { return false; }
	
	public default List<Accomplishment> getApprenticeAcc(){ return Lists.newArrayList(); }
	public default List<Accomplishment> getMasterAcc(){ return Lists.newArrayList(); }
	
	public Identifier registryName();
	
	public default void readFromJson(JsonObject json) { };
	
	public default JsonObject writeToJson(JsonObject obj)
	{
		obj.addProperty("EntityType", registryName().toString());
		return obj;
	}
}
