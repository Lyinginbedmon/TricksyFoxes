package com.lying.tricksy.init;

import com.lying.tricksy.component.TricksyComponent;
import com.lying.tricksy.reference.Reference;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import net.minecraft.entity.mob.MobEntity;

public class TFComponents implements EntityComponentInitializer
{
	public static final ComponentKey<TricksyComponent> TRICKSY_TRACKING = ComponentRegistry.getOrCreate(Reference.ModInfo.prefix("tricksy_tracking"), TricksyComponent.class);
	
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry)
	{
		registry.registerFor(MobEntity.class, TRICKSY_TRACKING, TricksyComponent::new);
	}
}
