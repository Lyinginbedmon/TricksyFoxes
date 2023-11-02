package com.lying.tricksy.init;

import java.util.HashMap;
import java.util.Map;

import com.lying.tricksy.block.entity.ClockworkFriarBlockEntity;
import com.lying.tricksy.block.entity.WorkTableBlockEntity;
import com.lying.tricksy.reference.Reference;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class TFBlockEntities
{
	private static final Map<BlockEntityType<?>, Identifier> BLOCK_ENTITIES = new HashMap<>();
	
	public static final BlockEntityType<WorkTableBlockEntity> WORK_TABLE = register("work_table", FabricBlockEntityTypeBuilder.create(WorkTableBlockEntity::new, TFBlocks.WORK_TABLE).build(null));
	public static final BlockEntityType<ClockworkFriarBlockEntity> CLOCKWORK_FRIAR = register("clockwork_friar", FabricBlockEntityTypeBuilder.create(ClockworkFriarBlockEntity::new, TFBlocks.CLOCKWORK_FRIAR).build(null));
	
	private static <T extends BlockEntity> BlockEntityType<T> register(String nameIn, BlockEntityType<T> typeIn)
	{
		BLOCK_ENTITIES.put(typeIn, new Identifier(Reference.ModInfo.MOD_ID, nameIn));
		return typeIn;
	}
	
	public static void init()
	{
		BLOCK_ENTITIES.keySet().forEach(blockEntityType -> Registry.register(Registries.BLOCK_ENTITY_TYPE, BLOCK_ENTITIES.get(blockEntityType), blockEntityType));
	}
}
