package com.lying.tricksy.data;

import java.util.function.Consumer;

import com.lying.tricksy.init.TFItems;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;

public class TFRecipeProvider extends FabricRecipeProvider
{
	public TFRecipeProvider(FabricDataOutput output)
	{
		super(output);
	}
	
	// FIXME Recipe does not load or unlock ingame? Unreliable
	public void generate(Consumer<RecipeJsonProvider> exporter)
	{
		ShapelessRecipeJsonBuilder.create(RecipeCategory.COMBAT, TFItems.SAGE_HAT).input(Items.LEATHER).input(Items.AMETHYST_SHARD)
			.criterion(FabricRecipeProvider.hasItem(Items.LEATHER), FabricRecipeProvider.conditionsFromItem(Items.LEATHER))
			.criterion(FabricRecipeProvider.hasItem(Items.AMETHYST_SHARD), FabricRecipeProvider.conditionsFromItem(Items.AMETHYST_SHARD))
			.criterion(FabricRecipeProvider.hasItem(TFItems.SAGE_HAT), FabricRecipeProvider.conditionsFromItem(TFItems.SAGE_HAT)).offerTo(exporter);
	}
}
