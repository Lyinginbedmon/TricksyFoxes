package com.lying.tricksy.init;

import com.lying.tricksy.component.Accomplishment;
import com.lying.tricksy.entity.ai.node.NodeType;
import com.lying.tricksy.reference.Reference;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class TFRegistries
{
	public static final RegistryKey<Registry<Accomplishment>> ACC_KEY = RegistryKey.ofRegistry(new Identifier(Reference.ModInfo.MOD_ID, "accomplishment"));
	public static final Registry<Accomplishment> ACC_REGISTRY = FabricRegistryBuilder.createSimple(ACC_KEY).buildAndRegister();
	
	public static final RegistryKey<Registry<TFObjType<?>>> OBJ_KEY = RegistryKey.ofRegistry(new Identifier(Reference.ModInfo.MOD_ID, "object_type"));
	public static final Registry<TFObjType<?>> OBJ_REGISTRY = FabricRegistryBuilder.createSimple(OBJ_KEY).buildAndRegister();
	
	public static final RegistryKey<Registry<NodeType<?>>> TYPE_KEY = RegistryKey.ofRegistry(new Identifier(Reference.ModInfo.MOD_ID, "node_supertype"));
	public static final Registry<NodeType<?>> TYPE_REGISTRY = FabricRegistryBuilder.createSimple(TYPE_KEY).buildAndRegister();
	
	public static void init()
	{
		TFAccomplishments.init();
		TFObjType.init();
		TFNodeTypes.init();
	}
}
