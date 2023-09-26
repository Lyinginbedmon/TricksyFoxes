package com.lying.tricksy.data.recipe;

import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.util.Identifier;

public abstract class RecipeNote extends SpecialCraftingRecipe
{
	protected RecipeNote(Identifier id)
	{
		super(id, CraftingRecipeCategory.MISC);
	}
	
	public boolean fits(int var1, int var2) { return var1 * var2 > 1; }
	
	public RecipeType<?> getType() { return RecipeType.CRAFTING; }
}
