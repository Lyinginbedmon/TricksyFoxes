package com.lying.tricksy.data.recipe;

import java.util.function.Supplier;

import com.google.gson.JsonObject;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;

/**
 * A simplistic recipe serializer class for recipes that don't require additional data
 * @author Lying
 */
public class SerializerSimple<T extends Recipe<?>> implements RecipeSerializer<T>
{
	private final Supplier<T> constructor;
	
	public SerializerSimple(Supplier<T> constructorIn)
	{
		this.constructor = constructorIn;
	}
	
    // Turns json into Recipe
    public T read(Identifier id, JsonObject json) { return constructor.get(); }
    
    // Turns Recipe into PacketByteBuf
    public void write(PacketByteBuf packetData, T recipe) { }
    
    // Turns PacketByteBuf into Recipe
    public T read(Identifier id, PacketByteBuf packetData) { return constructor.get(); }
}
