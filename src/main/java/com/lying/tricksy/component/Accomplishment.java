package com.lying.tricksy.component;

import java.util.function.Predicate;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

public class Accomplishment
{
	private final Identifier name;
	
	private Predicate<MobEntity> conditions;
	
	public Accomplishment(Identifier nameIn)
	{
		this.name = nameIn;
	}
	
	public final Identifier registryName() { return name; }
	
	public final Accomplishment condition(Predicate<MobEntity> conditionIn)
	{
		if(conditions == null)
			conditions = conditionIn;
		else
			conditions = conditions.and(conditionIn);
		return this;
	}
	
	public final boolean achieved(MobEntity entity) { return conditions == null ? true : conditions.test(entity); }
}
