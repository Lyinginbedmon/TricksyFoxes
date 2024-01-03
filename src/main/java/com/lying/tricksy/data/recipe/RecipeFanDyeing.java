package com.lying.tricksy.data.recipe;

import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.init.TFSpecialRecipes;
import com.lying.tricksy.item.ItemSageFan;
import com.lying.tricksy.reference.Reference;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class RecipeFanDyeing extends SpecialCraftingRecipe
{
	public static final Identifier ID = new Identifier(Reference.ModInfo.MOD_ID, "fan_dyeing");
	
	public RecipeFanDyeing()
	{
		super(ID, CraftingRecipeCategory.MISC);
	}
	
	public boolean fits(int var1, int var2) { return var1 * var2 >= 2; }
	
	public RecipeType<?> getType() { return RecipeType.CRAFTING; }
	
	public boolean matches(RecipeInputInventory var1, World var2)
	{
		ItemStack fan = ItemStack.EMPTY;
		ItemStack dye = ItemStack.EMPTY;
		for(int i=0; i<var1.size(); i++)
		{
			ItemStack stack = var1.getStack(i);
			if(stack.getItem() instanceof ItemSageFan)
			{
				if(fan.isEmpty())
					fan = stack.copy();
				else
					return false;
			}
			else if(stack.getItem() instanceof DyeItem)
			{
				if(dye.isEmpty())
					dye = stack.copy();
				else
					return false;
			}
		}
		
		return !fan.isEmpty() && !dye.isEmpty();
	}
	
	public ItemStack craft(RecipeInputInventory var1, DynamicRegistryManager var2)
	{
		ItemStack fan = ItemStack.EMPTY;
		ItemStack dye = ItemStack.EMPTY;
		for(int i=0; i<var1.size(); i++)
		{
			ItemStack stack = var1.getStack(i);
			if(stack.getItem() instanceof ItemSageFan)
			{
				if(fan.isEmpty())
					fan = stack.copy();
				else
					return ItemStack.EMPTY;
			}
			else if(stack.getItem() instanceof DyeItem)
			{
				if(dye.isEmpty())
					dye = stack.copy();
				else
					return ItemStack.EMPTY;
			}
		}
		
		if(!fan.isEmpty() && !dye.isEmpty())
		{
			DyeColor color = ((DyeItem)dye.getItem()).getColor();
			ItemStack product = TFItems.FAN_COLOR_MAP.get(color).getDefaultStack().copy();
			if(fan.hasNbt())
				product.setNbt(fan.getNbt());
			product.setDamage(0);
			return product;
		}
		
		return ItemStack.EMPTY;
	}
	
	public RecipeSerializer<?> getSerializer() { return TFSpecialRecipes.FAN_DYEING_SERIALIZER; }
}
