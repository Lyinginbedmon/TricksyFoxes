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
	public static final DefaultParticleType LEVELUP = FabricParticleTypes.simple();
	public static final DefaultParticleType AURA = FabricParticleTypes.simple();
	public static final DefaultParticleType ENERGY = FabricParticleTypes.simple();
	public static final DefaultParticleType ENERGY_EMITTER = FabricParticleTypes.simple();
	
	public static void init()
	{
		register("paper", PAPER);
		register("foxfire", FOXFIRE);
		register("levelup", LEVELUP);
		register("aura", AURA);
		register("energy", ENERGY);
		register("energy_emitter", ENERGY_EMITTER);
	}
	
	private static void register(String name, DefaultParticleType type)
	{
		Registry.register(Registries.PARTICLE_TYPE, new Identifier(Reference.ModInfo.MOD_ID, name), type);
	}
}
