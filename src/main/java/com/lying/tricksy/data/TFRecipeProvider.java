package com.lying.tricksy.data;

import java.util.function.Consumer;

import com.lying.tricksy.init.TFBlocks;
import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.init.TFSpecialRecipes;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.ComplexRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;

public class TFRecipeProvider extends FabricRecipeProvider
{
	public TFRecipeProvider(FabricDataOutput output)
	{
		super(output);
	}
	
	// XXX Recipe does not load or unlock ingame? Unreliable
	public void generate(Consumer<RecipeJsonProvider> exporter)
	{
		ComplexRecipeJsonBuilder.create(TFSpecialRecipes.NOTE_INTEGER_SERIALIZER).offerTo(exporter, "note_integer");
		ComplexRecipeJsonBuilder.create(TFSpecialRecipes.NOTE_BOOLEAN_SERIALIZER).offerTo(exporter, "note_boolean");
		ComplexRecipeJsonBuilder.create(TFSpecialRecipes.NOTE_ITEM_SERIALIZER).offerTo(exporter, "note_item");
		
		ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, TFItems.SAGE_HAT)
			.pattern("gag").pattern("l l")
			.input('l', Items.LEATHER).input('a',Items.AMETHYST_SHARD).input('g', Items.GOLD_INGOT)
			.criterion(FabricRecipeProvider.hasItem(Items.LEATHER), FabricRecipeProvider.conditionsFromItem(Items.LEATHER))
			.criterion(FabricRecipeProvider.hasItem(Items.AMETHYST_SHARD), FabricRecipeProvider.conditionsFromItem(Items.AMETHYST_SHARD))
			.criterion(FabricRecipeProvider.hasItem(Items.GOLD_INGOT), FabricRecipeProvider.conditionsFromItem(Items.GOLD_INGOT))
			.criterion(FabricRecipeProvider.hasItem(TFItems.SAGE_HAT), FabricRecipeProvider.conditionsFromItem(TFItems.SAGE_HAT)).offerTo(exporter);
		
		ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, TFItems.PERIAPT).input(TFBlocks.PRESCIENCE).input(Items.LEAD)
			.criterion(FabricRecipeProvider.hasItem(TFBlocks.PRESCIENCE), FabricRecipeProvider.conditionsFromItem(TFBlocks.PRESCIENCE))
			.criterion(FabricRecipeProvider.hasItem(Items.LEAD), FabricRecipeProvider.conditionsFromItem(Items.LEAD))
			.criterion(FabricRecipeProvider.hasItem(TFItems.PERIAPT), FabricRecipeProvider.conditionsFromItem(TFItems.PERIAPT)).offerTo(exporter);
		
		ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, TFBlocks.PRESCIENCE).input(Items.EXPERIENCE_BOTTLE).input(Items.AMETHYST_SHARD)
			.criterion(FabricRecipeProvider.hasItem(Items.EXPERIENCE_BOTTLE), FabricRecipeProvider.conditionsFromItem(Items.EXPERIENCE_BOTTLE))
			.criterion(FabricRecipeProvider.hasItem(Items.AMETHYST_SHARD), FabricRecipeProvider.conditionsFromItem(Items.AMETHYST_SHARD))
			.criterion(FabricRecipeProvider.hasItem(TFBlocks.PRESCIENCE), FabricRecipeProvider.conditionsFromItem(TFBlocks.PRESCIENCE)).offerTo(exporter);
	}
	
	public static void addBrewingRecipes()
	{
		;
	}
}
