package com.lying.tricksy.init;

import com.lying.tricksy.reference.Reference;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class TFParticles
{
	public static final DefaultParticleType PAPER	= FabricParticleTypes.simple();
	public static final DefaultParticleType FOXFIRE	= FabricParticleTypes.simple();
	
	public static void init()
	{
		Registry.register(Registries.PARTICLE_TYPE, new Identifier(Reference.ModInfo.MOD_ID, "paper"), PAPER);
		Registry.register(Registries.PARTICLE_TYPE, new Identifier(Reference.ModInfo.MOD_ID, "foxfire"), FOXFIRE);
	}
}
