package com.lying.tricksy.component;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.init.TFAccomplishments;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Identifier;

public abstract class ConfigurablePath<T extends MobEntity, N extends PathAwareEntity & ITricksyMob<?>> implements EnlightenmentPath<T, N>
{
	private final List<Accomplishment> stepsApprentice = Lists.newArrayList();
	private final List<Accomplishment> stepsMaster = Lists.newArrayList();
	private final Identifier registryName;
	
	public ConfigurablePath(EntityType<? extends T> typeIn, Accomplishment... stepsIn)
	{
		this.registryName = EntityType.getId(typeIn);
		for(Accomplishment step : stepsIn)
			if(!stepsApprentice.contains(step))
				stepsApprentice.add(step);
	}
	
	public ConfigurablePath<T,N> setMastery(Accomplishment... stepsIn)
	{
		stepsMaster.clear();
		for(Accomplishment step : stepsIn)
			if(!stepsMaster.contains(step))
				stepsMaster.add(step);
		return this;
	}
	
	public Identifier registryName() { return registryName; }
	
	public boolean conditionsMet(Collection<Accomplishment> accomplishments) { return matchesNonEmptyList(accomplishments, this.stepsApprentice); }
	
	public boolean hasReachedMastery(Collection<Accomplishment> accomplishments) { return matchesNonEmptyList(accomplishments, this.stepsMaster); }
	
	private static boolean matchesNonEmptyList(Collection<Accomplishment> accomplishments, List<Accomplishment> list)
	{
		return !list.isEmpty() && list.stream().allMatch(acc -> accomplishments.contains(acc));
	}
	
	public List<Accomplishment> getApprenticeAcc(){ return this.stepsApprentice; }
	public List<Accomplishment> getMasterAcc(){ return this.stepsMaster; }
	
	public void readFromJson(JsonObject dataIn)
	{
		stepsApprentice.clear();
		if(dataIn.has("Accomplishments"))
			readJsonToList(dataIn.getAsJsonArray("Accomplishments"), this.stepsApprentice);
		
		if(dataIn.has("Mastery"))
			readJsonToList(dataIn.getAsJsonArray("Mastery"), this.stepsMaster);
	}
	
	public JsonObject writeToJson(JsonObject obj)
	{
		EnlightenmentPath.super.writeToJson(obj);
		
		if(!this.stepsApprentice.isEmpty())
			obj.add("Accomplishments", writeListToJson(this.stepsApprentice));
		if(!this.stepsMaster.isEmpty())
			obj.add("Mastery", writeListToJson(this.stepsMaster));
		return obj;
	}
	
	private static void readJsonToList(JsonArray stepsIn, List<Accomplishment> list)
	{
		for(int i=0; i<stepsIn.size(); i++)
		{
			String entry = stepsIn.get(i).getAsString();
			Accomplishment acc = TFAccomplishments.get(new Identifier(entry));
			if(acc != null && !list.contains(acc))
				list.add(acc);
		}
	}
	
	private static JsonArray writeListToJson(List<Accomplishment> list)
	{
		JsonArray steps = new JsonArray();
		list.forEach(acc -> steps.add(acc.registryName().toString()));
		return steps;
	}
}
