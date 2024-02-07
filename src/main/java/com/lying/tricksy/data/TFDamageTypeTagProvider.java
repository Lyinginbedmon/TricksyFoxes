package com.lying.tricksy.data;

import java.util.concurrent.CompletableFuture;

import com.lying.tricksy.init.TFDamageTypes;

import net.minecraft.data.DataOutput;
import net.minecraft.data.server.tag.TagProvider;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.DamageTypeTags;

public class TFDamageTypeTagProvider extends TagProvider<DamageType>
{
	public TFDamageTypeTagProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> maxChainedNeighborUpdates)
	{
		super(output, RegistryKeys.DAMAGE_TYPE, maxChainedNeighborUpdates);
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
