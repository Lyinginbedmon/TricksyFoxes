package com.lying.tricksy.init;

import java.util.HashMap;
import java.util.Map;

import com.lying.tricksy.data.recipe.RecipeFanDyeing;
import com.lying.tricksy.data.recipe.RecipeNoteBool;
import com.lying.tricksy.data.recipe.RecipeNoteEntity;
import com.lying.tricksy.data.recipe.RecipeNoteInteger;
import com.lying.tricksy.data.recipe.RecipeNoteItem;
import com.lying.tricksy.data.recipe.RecipeNoteRegion1;
import com.lying.tricksy.data.recipe.RecipeNoteRegion2;
import com.lying.tricksy.data.recipe.RecipeNoteSeal;
import com.lying.tricksy.data.recipe.SerializerSimple;
import com.lying.tricksy.reference.Reference;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class TFSpecialRecipes
{
	private static final Map<RecipeSerializer<?>, Identifier> RECIPE_SERIALIZERS = new HashMap<>();
	private static final Map<RecipeType<?>, Identifier> RECIPE_TYPES = new HashMap<>();
	
	public static final RecipeSerializer<RecipeNoteInteger> NOTE_INTEGER_SERIALIZER = makeSerializer("note_integer", new SerializerSimple<RecipeNoteInteger>(RecipeNoteInteger::new));
	public static final RecipeSerializer<RecipeNoteBool> NOTE_BOOLEAN_SERIALIZER = makeSerializer("note_boolean", new SerializerSimple<RecipeNoteBool>(RecipeNoteBool::new));
	public static final RecipeSerializer<RecipeNoteEntity> NOTE_ENTITY_SERIALIZER = makeSerializer("note_entity", new SerializerSimple<RecipeNoteEntity>(RecipeNoteEntity::new));
	public static final RecipeSerializer<RecipeNoteItem> NOTE_ITEM_SERIALIZER = makeSerializer("note_item", new SerializerSimple<RecipeNoteItem>(RecipeNoteItem::new));
	public static final RecipeSerializer<RecipeNoteRegion1> NOTE_MAKE_REGION_SERIALIZER = makeSerializer("note_create_region", new SerializerSimple<RecipeNoteRegion1>(RecipeNoteRegion1::new));
	public static final RecipeSerializer<RecipeNoteRegion2> NOTE_REGION_SERIALIZER = makeSerializer("note_region", new SerializerSimple<RecipeNoteRegion2>(RecipeNoteRegion2::new));
	public static final RecipeSerializer<RecipeNoteSeal> NOTE_SEALING_SERIALIZER = makeSerializer("note_sealing", new SerializerSimple<RecipeNoteSeal>(RecipeNoteSeal::new));
	public static final RecipeSerializer<RecipeFanDyeing> FAN_DYEING_SERIALIZER = makeSerializer("fan_dyeing", new SerializerSimple<RecipeFanDyeing>(RecipeFanDyeing::new));
	
	static <T extends Recipe<?>> RecipeSerializer<T> makeSerializer(String name, RecipeSerializer<T> serializer)
	{
		RECIPE_SERIALIZERS.put(serializer, new Identifier(Reference.ModInfo.MOD_ID, name));
		return serializer;
	}
	
	@SuppressWarnings("unused")
	private static <T extends Recipe<?>> RecipeType<T> makeType(String name)
	{
		RecipeType<T> type = new RecipeType<>() { public String toString() { return name; } };
		RECIPE_TYPES.put(type, new Identifier(Reference.ModInfo.MOD_ID, name));
		return type;
	}
	
	public static void init()
	{
		RECIPE_SERIALIZERS.keySet().forEach(serializer -> Registry.register(Registries.RECIPE_SERIALIZER, RECIPE_SERIALIZERS.get(serializer), serializer));
		RECIPE_TYPES.keySet().forEach(type -> Registry.register(Registries.RECIPE_TYPE, RECIPE_TYPES.get(type), type));
	}
}
