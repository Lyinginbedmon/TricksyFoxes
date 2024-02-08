package com.lying.tricksy.init;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.damage.DamageEffects;
import net.minecraft.entity.damage.DamageScaling;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class TFDamageTypes
{
	private static final Map<RegistryKey<DamageType>, DamageType> TYPES = new HashMap<>();
	
	/** A kind of magical fire whose damage ignores damage cooldowns */
	public static final RegistryKey<DamageType> FOXFIRE = register(new DamageType("foxfire", DamageScaling.NEVER, 0F, DamageEffects.BURNING));
	
	public static RegistryKey<DamageType> register(DamageType source)
	{
		RegistryKey<DamageType> type = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(Reference.ModInfo.MOD_ID, source.msgId()));
		TYPES.put(type, source);
		return type;
	}
	
	public static void init() { }
	
	public static void bootstrap(Registerable<DamageType> registerable)
	{
		registerable.register(FOXFIRE, TYPES.get(FOXFIRE));
	}
	
	public static Collection<DamageType> sources() { return TYPES.values(); }
	
	public static DamageSource of(World world, RegistryKey<DamageType> key) { return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key)); }
}
