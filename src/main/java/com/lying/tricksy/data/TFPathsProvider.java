package com.lying.tricksy.data;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.lying.tricksy.init.TFEnlightenmentPaths;
import com.lying.tricksy.reference.Reference;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataOutput.OutputType;
import net.minecraft.data.DataOutput.PathResolver;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.util.Identifier;

public class TFPathsProvider implements DataProvider
{
	private final PathResolver path;
	
	public TFPathsProvider(FabricDataOutput generator)
	{
		this.path = generator.getResolver(OutputType.DATA_PACK, TFEnlightenmentPaths.FILE_PATH+"/");
	}
	
	public CompletableFuture<?> run(DataWriter dataWriter)
	{
		List<CompletableFuture<?>> futures = Lists.newArrayList();
		TFEnlightenmentPaths.getDefaultPaths().forEach(path -> 
			futures.add(DataProvider.writeToPath(dataWriter, path.writeToJson(new JsonObject()), this.path.resolveJson(new Identifier(Reference.ModInfo.MOD_ID, path.registryName().getPath())))));
		return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
	}
	
	public String getName()
	{
		return "Tricksy Foxes enlightenment paths";
	}
	
}
