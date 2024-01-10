package com.lying.tricksy.init;

import com.lying.tricksy.entity.EntityFoxFire;
import com.lying.tricksy.entity.EntityTricksyFox;
import com.lying.tricksy.entity.EntityTricksyGoat;
import com.lying.tricksy.entity.EntityTricksyWolf;
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
	
	public static final EntityType<EntityTricksyGoat> TRICKSY_GOAT = Registry.register(
			Registries.ENTITY_TYPE, 
			new Identifier(Reference.ModInfo.MOD_ID, "tricksy_goat"), 
			FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, EntityTricksyGoat::new).dimensions(EntityDimensions.fixed(0.8F, 1.85F)).build());
	
	public static final EntityType<EntityTricksyWolf> TRICKSY_WOLF = Registry.register(
			Registries.ENTITY_TYPE,
			new Identifier(Reference.ModInfo.MOD_ID, "tricksy_wolf"),
			FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, EntityTricksyWolf::new).dimensions(EntityDimensions.fixed(0.6F, 1.7F)).build());
	
	public static final EntityType<EntityFoxFire> FOX_FIRE = Registry.register(
			Registries.ENTITY_TYPE,
			new Identifier(Reference.ModInfo.MOD_ID, "fox_fire"),
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, EntityFoxFire::new).dimensions(EntityDimensions.fixed(0.2F, 0.2F)).build());
	
	public static void init()
	{
		FabricDefaultAttributeRegistry.register(TRICKSY_FOX, EntityTricksyFox.createMobAttributes());
		FabricDefaultAttributeRegistry.register(TRICKSY_GOAT, EntityTricksyGoat.createMobAttributes());
		FabricDefaultAttributeRegistry.register(TRICKSY_WOLF, EntityTricksyWolf.createMobAttributes());
	}
}
