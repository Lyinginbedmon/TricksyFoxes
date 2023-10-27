package com.lying.tricksy.data;

import java.util.concurrent.CompletableFuture;

import com.lying.tricksy.reference.Reference;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.EntityTypeTagProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class TFEntityTags extends EntityTypeTagProvider
{
	public static final TagKey<EntityType<?>> MONSTER = TagKey.of(RegistryKeys.ENTITY_TYPE, new Identifier(Reference.ModInfo.MOD_ID, "monster"));
	public static final TagKey<EntityType<?>> ANIMAL = TagKey.of(RegistryKeys.ENTITY_TYPE, new Identifier(Reference.ModInfo.MOD_ID, "animal"));
	
	public TFEntityTags(FabricDataOutput output, CompletableFuture<WrapperLookup> completableFuture)
	{
		super(output, completableFuture);
	}
	
	protected void configure(WrapperLookup arg)
	{
		getOrCreateTagBuilder(MONSTER).add(
				EntityType.BLAZE, 
				EntityType.CAVE_SPIDER, 
				EntityType.CREEPER, 
				EntityType.DROWNED, 
				EntityType.ELDER_GUARDIAN,
				EntityType.ENDER_DRAGON,
				EntityType.ENDERMAN,
				EntityType.ENDERMITE,
				EntityType.EVOKER,
				EntityType.GHAST,
				EntityType.GIANT,
				EntityType.GUARDIAN,
				EntityType.HOGLIN,
				EntityType.HUSK,
				EntityType.ILLUSIONER,
				EntityType.MAGMA_CUBE,
				EntityType.PHANTOM,
				EntityType.PIGLIN_BRUTE,
				EntityType.PILLAGER,
				EntityType.RAVAGER,
				EntityType.SHULKER,
				EntityType.SILVERFISH,
				EntityType.SKELETON,
				EntityType.SLIME,
				EntityType.SPIDER,
				EntityType.STRAY,
				EntityType.VEX,
				EntityType.VINDICATOR,
				EntityType.WARDEN,
				EntityType.WITCH,
				EntityType.WITHER,
				EntityType.WITHER_SKELETON,
				EntityType.ZOGLIN,
				EntityType.ZOMBIE,
				EntityType.ZOMBIE_VILLAGER);
		
		getOrCreateTagBuilder(ANIMAL).add(
				EntityType.CHICKEN,
				EntityType.COD,
				EntityType.COW,
				EntityType.GOAT,
				EntityType.MOOSHROOM,
				EntityType.PIG,
				EntityType.PUFFERFISH,
				EntityType.RABBIT,
				EntityType.SALMON,
				EntityType.SHEEP,
				EntityType.TROPICAL_FISH,
				EntityType.TURTLE);
	}
}
