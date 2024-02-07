package com.lying.tricksy.data;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.lying.tricksy.init.TFDamageTypes;
import com.lying.tricksy.reference.Reference;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataOutput.OutputType;
import net.minecraft.data.DataOutput.PathResolver;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.util.Identifier;

public class TFDamageTypesProvider implements DataProvider
{
	public static final String FILE_PATH = "damage_type";
	
	private final PathResolver path;
	
	public TFDamageTypesProvider(FabricDataOutput generator)
	{
		this.path = generator.getResolver(OutputType.DATA_PACK, FILE_PATH+"/");
	}
	
	public CompletableFuture<?> run(DataWriter dataWriter)
	{
		List<CompletableFuture<?>> futures = Lists.newArrayList();
		
		TFDamageTypes.sources().forEach(type -> 
		{
			JsonObject obj = new JsonObject();
			obj.addProperty("message_id", type.msgId());
			obj.addProperty("exhaustion", type.exhaustion());
			obj.addProperty("scaling", type.scaling().asString());
			obj.addProperty("effects", type.effects().asString());
			obj.addProperty("death_message_type", type.deathMessageType().asString());
			futures.add(DataProvider.writeToPath(dataWriter, obj, this.path.resolveJson(new Identifier(Reference.ModInfo.MOD_ID, type.msgId()))));
		});
		return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
	}
	
	public String getName()
	{
		return "Tricksy Foxes damage types";
	}
}
