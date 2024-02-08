package com.lying.tricksy.data;

import java.util.concurrent.CompletableFuture;

import com.lying.tricksy.init.TFDamageTypes;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.DamageTypeTags;

public class TFDamageTypeTagProvider extends FabricTagProvider<DamageType>
{
	public TFDamageTypeTagProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> registriesFuture)
	{
		super(output, RegistryKeys.DAMAGE_TYPE, registriesFuture);
	}

	protected void configure(RegistryWrapper.WrapperLookup lookup)
	{
		this.getOrCreateTagBuilder(DamageTypeTags.BYPASSES_COOLDOWN).add(TFDamageTypes.FOXFIRE);
	}
	
	public String getName()
	{
		return "Tricksy Foxes damage type tags";
	}
}
