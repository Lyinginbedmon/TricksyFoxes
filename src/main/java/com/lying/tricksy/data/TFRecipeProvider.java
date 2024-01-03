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
		ComplexRecipeJsonBuilder.create(TFSpecialRecipes.NOTE_ENTITY_SERIALIZER).offerTo(exporter, "note_entity");
		ComplexRecipeJsonBuilder.create(TFSpecialRecipes.NOTE_ITEM_SERIALIZER).offerTo(exporter, "note_item");
		ComplexRecipeJsonBuilder.create(TFSpecialRecipes.NOTE_REGION_SERIALIZER).offerTo(exporter, "note_region");
		ComplexRecipeJsonBuilder.create(TFSpecialRecipes.NOTE_MAKE_REGION_SERIALIZER).offerTo(exporter, "note_create_region");
		ComplexRecipeJsonBuilder.create(TFSpecialRecipes.NOTE_SEALING_SERIALIZER).offerTo(exporter, "note_sealing");
		ComplexRecipeJsonBuilder.create(TFSpecialRecipes.FAN_DYEING_SERIALIZER).offerTo(exporter, "fan_dyeing");
		
		ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, TFItems.SAGE_HAT)
			.pattern("gag").pattern("l l")
			.input('l', Items.LEATHER).input('a',Items.AMETHYST_SHARD).input('g', Items.GOLD_INGOT)
			.criterion(FabricRecipeProvider.hasItem(Items.LEATHER), FabricRecipeProvider.conditionsFromItem(Items.LEATHER))
			.criterion(FabricRecipeProvider.hasItem(Items.AMETHYST_SHARD), FabricRecipeProvider.conditionsFromItem(Items.AMETHYST_SHARD))
			.criterion(FabricRecipeProvider.hasItem(Items.GOLD_INGOT), FabricRecipeProvider.conditionsFromItem(Items.GOLD_INGOT))
			.criterion(FabricRecipeProvider.hasItem(TFItems.SAGE_HAT), FabricRecipeProvider.conditionsFromItem(TFItems.SAGE_HAT)).offerTo(exporter);
		
		ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, TFItems.SAGE_FAN)
			.pattern("fp").pattern("sf")
			.input('f', Items.FEATHER).input('s', Items.STICK).input('p', Items.PAPER)
			.criterion(FabricRecipeProvider.hasItem(Items.FEATHER), FabricRecipeProvider.conditionsFromItem(Items.FEATHER))
			.criterion(FabricRecipeProvider.hasItem(Items.PAPER), FabricRecipeProvider.conditionsFromItem(Items.PAPER))
			.criterion(FabricRecipeProvider.hasItem(Items.STICK), FabricRecipeProvider.conditionsFromItem(Items.STICK)).offerTo(exporter);
		
		ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, TFItems.PERIAPT).input(TFBlocks.PRESCIENCE).input(Items.LEAD)
			.criterion(FabricRecipeProvider.hasItem(TFBlocks.PRESCIENCE), FabricRecipeProvider.conditionsFromItem(TFBlocks.PRESCIENCE))
			.criterion(FabricRecipeProvider.hasItem(Items.LEAD), FabricRecipeProvider.conditionsFromItem(Items.LEAD))
			.criterion(FabricRecipeProvider.hasItem(TFItems.PERIAPT), FabricRecipeProvider.conditionsFromItem(TFItems.PERIAPT)).offerTo(exporter);
		
		ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, TFBlocks.PRESCIENCE).input(Items.EXPERIENCE_BOTTLE).input(Items.AMETHYST_SHARD)
			.criterion(FabricRecipeProvider.hasItem(Items.EXPERIENCE_BOTTLE), FabricRecipeProvider.conditionsFromItem(Items.EXPERIENCE_BOTTLE))
			.criterion(FabricRecipeProvider.hasItem(Items.AMETHYST_SHARD), FabricRecipeProvider.conditionsFromItem(Items.AMETHYST_SHARD))
			.criterion(FabricRecipeProvider.hasItem(TFBlocks.PRESCIENCE), FabricRecipeProvider.conditionsFromItem(TFBlocks.PRESCIENCE)).offerTo(exporter);
		
		ShapelessRecipeJsonBuilder.create(RecipeCategory.REDSTONE, TFBlocks.PRESCIENT_CANDLE, 2).input(TFBlocks.PRESCIENCE).input(Items.STRING).input(Items.HONEYCOMB)
			.criterion(FabricRecipeProvider.hasItem(Items.STRING), FabricRecipeProvider.conditionsFromItem(Items.STRING))
			.criterion(FabricRecipeProvider.hasItem(Items.HONEYCOMB), FabricRecipeProvider.conditionsFromItem(Items.HONEYCOMB))
			.criterion(FabricRecipeProvider.hasItem(TFBlocks.PRESCIENCE), FabricRecipeProvider.conditionsFromItem(TFBlocks.PRESCIENCE)).offerTo(exporter);
		
		ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, TFItems.SCRIPTURE)
			.pattern("ppp")
			.pattern("pbp")
			.pattern("ppp")
			.input('p', TFItems.NOTE).input('b', Items.WRITABLE_BOOK)
			.criterion(FabricRecipeProvider.hasItem(TFItems.NOTE), FabricRecipeProvider.conditionsFromItem(TFItems.NOTE))
			.criterion(FabricRecipeProvider.hasItem(Items.WRITABLE_BOOK), FabricRecipeProvider.conditionsFromItem(Items.WRITABLE_BOOK)).offerTo(exporter);
		
		ShapelessRecipeJsonBuilder.create(RecipeCategory.REDSTONE, TFBlocks.WORK_TABLE).input(Items.CRAFTING_TABLE).input(Items.BARREL)
			.criterion(FabricRecipeProvider.hasItem(Items.CRAFTING_TABLE), FabricRecipeProvider.conditionsFromItem(Items.CRAFTING_TABLE))
			.criterion(FabricRecipeProvider.hasItem(Items.BARREL), FabricRecipeProvider.conditionsFromItem(Items.BARREL)).offerTo(exporter);
		
		ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, TFBlocks.CLOCKWORK_FRIAR)
			.pattern(" s")
			.pattern("wa")
			.input('s', TFItemTags.SKULLS).input('w', TFBlocks.WORK_TABLE).input('a', Items.ARMOR_STAND)
			.criterion(FabricRecipeProvider.hasItem(TFBlocks.WORK_TABLE), FabricRecipeProvider.conditionsFromItem(TFBlocks.WORK_TABLE))
			.criterion(FabricRecipeProvider.hasItem(Items.ARMOR_STAND), FabricRecipeProvider.conditionsFromItem(Items.ARMOR_STAND)).offerTo(exporter);
	}
	
	public static void addBrewingRecipes()
	{
		;
	}
}
