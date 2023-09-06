package com.lying.tricksy.init;

import com.lying.tricksy.entity.EntityTricksyFox;
import com.lying.tricksy.reference.Reference;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class TFEntityTypes
{
	public static final EntityType<EntityTricksyFox> TRICKSY_FOX = Registry.register(
			Registries.ENTITY_TYPE, 
			new Identifier(Reference.ModInfo.MOD_ID, "tricksy_fox"), 
			FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, EntityTricksyFox::new).dimensions(EntityDimensions.fixed(0.75F, 1.5F)).build());
	
	public static void init()
	{
		FabricDefaultAttributeRegistry.register(TRICKSY_FOX, EntityTricksyFox.createMobAttributes());
	}
}
