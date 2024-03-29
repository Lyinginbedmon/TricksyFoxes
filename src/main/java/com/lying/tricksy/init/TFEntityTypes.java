package com.lying.tricksy.init;

import com.lying.tricksy.entity.EntityOnryoji;
import com.lying.tricksy.entity.EntitySeclusion;
import com.lying.tricksy.entity.EntityTricksyFox;
import com.lying.tricksy.entity.EntityTricksyGoat;
import com.lying.tricksy.entity.EntityTricksyWolf;
import com.lying.tricksy.entity.projectile.EntityFoxFire;
import com.lying.tricksy.entity.projectile.EntityOfudaStuck;
import com.lying.tricksy.entity.projectile.EntityOfudaThrown;
import com.lying.tricksy.entity.projectile.EntityOnryojiFire;
import com.lying.tricksy.reference.Reference;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class TFEntityTypes
{
	public static final EntityType<EntityTricksyFox> TRICKSY_FOX = register("tricksy_fox", 
			FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, EntityTricksyFox::new).dimensions(EntityDimensions.fixed(0.75F, 1.5F)).build());
	
	public static final EntityType<EntityTricksyGoat> TRICKSY_GOAT = register("tricksy_goat", 
			FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, EntityTricksyGoat::new).dimensions(EntityDimensions.fixed(0.8F, 1.85F)).build());
	
	public static final EntityType<EntityTricksyWolf> TRICKSY_WOLF = register("tricksy_wolf",
			FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, EntityTricksyWolf::new).dimensions(EntityDimensions.fixed(0.6F, 1.7F)).build());
	
	public static final EntityType<EntityOnryoji> ONRYOJI = register("onryoji",
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, EntityOnryoji::new).dimensions(EntityDimensions.fixed(1.5F, 1.5F)).build());
	
	public static final EntityType<EntityFoxFire> FOX_FIRE = register("fox_fire",
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, EntityFoxFire::new).dimensions(EntityDimensions.fixed(0.2F, 0.2F)).build());
	
	public static final EntityType<EntityOnryojiFire> ONRYOJI_FIRE = register("onryoji_fire",
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, EntityOnryojiFire::new).dimensions(EntityDimensions.fixed(0.6F, 0.6F)).build());
	
	public static final EntityType<EntityOfudaThrown> OFUDA_THROWN = register("ofuda_thrown",
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, EntityOfudaThrown::new).dimensions(EntityDimensions.fixed(0.5F, 0.5F)).build());
	
	public static final EntityType<EntityOfudaStuck> OFUDA_STUCK = register("ofuda_stuck",
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, EntityOfudaStuck::new).dimensions(EntityDimensions.fixed(0.2F, 0.2F)).build());
	
	public static final EntityType<EntitySeclusion> SECLUSION = register("seclusion", 
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, EntitySeclusion::new).dimensions(EntityDimensions.fixed(0.5F, 0.5F)).build());
	
	private static <T extends Entity> EntityType<T> register(String name, EntityType<T> entry)
	{
		return Registry.register(
				Registries.ENTITY_TYPE,
				new Identifier(Reference.ModInfo.MOD_ID, name),
				entry);
	}
	
	public static void init()
	{
		FabricDefaultAttributeRegistry.register(TRICKSY_FOX, EntityTricksyFox.createMobAttributes());
		FabricDefaultAttributeRegistry.register(TRICKSY_GOAT, EntityTricksyGoat.createMobAttributes());
		FabricDefaultAttributeRegistry.register(TRICKSY_WOLF, EntityTricksyWolf.createMobAttributes());
		FabricDefaultAttributeRegistry.register(ONRYOJI, EntityOnryoji.createOnryojiAttributes());
		FabricDefaultAttributeRegistry.register(OFUDA_STUCK, EntityOfudaStuck.createOfudaAttributes());
	}
}
