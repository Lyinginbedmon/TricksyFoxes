package com.lying.tricksy.component;

import java.util.Collection;

import com.google.gson.JsonObject;
import com.lying.tricksy.api.entity.ITricksyMob;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Identifier;

public interface EnlightenmentPath<T extends MobEntity, N extends PathAwareEntity & ITricksyMob<?>>
{
	@SuppressWarnings("unchecked")
	public default N giveEnlightenment(MobEntity entityIn) { return enlighten((T)entityIn); }
	
	public N enlighten(T entityIn);
	
	public boolean conditionsMet(Collection<Accomplishment> accomplishments);
	
	public Identifier registryName();
	
	public default void readFromJson(JsonObject json) { };
	
	public default JsonObject writeToJson(JsonObject obj)
	{
		obj.addProperty("EntityType", registryName().toString());
		return obj;
	}
}
