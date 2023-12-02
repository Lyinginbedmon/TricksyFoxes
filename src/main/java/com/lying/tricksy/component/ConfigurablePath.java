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
	private final List<Accomplishment> steps = Lists.newArrayList();
	private final Identifier registryName;
	
	public ConfigurablePath(EntityType<? extends T> typeIn, Accomplishment... stepsIn)
	{
		this.registryName = EntityType.getId(typeIn);
		for(Accomplishment step : stepsIn)
			if(!steps.contains(step))
				steps.add(step);
	}
	
	public Identifier registryName() { return registryName; }
	
	public boolean conditionsMet(Collection<Accomplishment> accomplishments)
	{
		return !steps.isEmpty() && steps.stream().allMatch(acc -> accomplishments.contains(acc));
	}
	
	public void readFromJson(JsonObject dataIn)
	{
		steps.clear();
		if(dataIn.has("Accomplishments"))
		{
			JsonArray stepsIn = dataIn.getAsJsonArray("Accomplishments");
			for(int i=0; i<stepsIn.size(); i++)
			{
				String entry = stepsIn.get(i).getAsString();
				Accomplishment acc = TFAccomplishments.get(new Identifier(entry));
				if(acc != null && !steps.contains(acc))
					steps.add(acc);
			}
		}
	}
	
	public JsonObject writeToJson(JsonObject obj)
	{
		EnlightenmentPath.super.writeToJson(obj);
		
		JsonArray steps = new JsonArray();
		this.steps.forEach(acc -> steps.add(acc.registryName().toString()));
		obj.add("Accomplishments", steps);
		return obj;
	}
}
