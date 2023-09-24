package com.lying.tricksy.data.recipe;

import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObj;
import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.init.TFSpecialRecipes;
import com.lying.tricksy.item.ItemPrescientNote;
import com.lying.tricksy.reference.Reference;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class RecipeNoteItem extends SpecialCraftingRecipe
{
	public static final Identifier ID = new Identifier(Reference.ModInfo.MOD_ID, "note_item");
	
	public RecipeNoteItem()
	{
		super(ID, CraftingRecipeCategory.MISC);
	}
	
	public boolean matches(RecipeInputInventory var1, World var2)
	{
		System.out.println("Matching against recipe "+ID.toString());
		ItemStack note = ItemStack.EMPTY;
		ItemStack dye = ItemStack.EMPTY;
		for(int i=0; i<var1.size(); i++)
		{
			ItemStack stackInSlot = var1.getStack(i);
			if(stackInSlot.isEmpty())
				continue;
			if(stackInSlot.getItem() == TFItems.NOTE_ITEM)
			{
				if(note.isEmpty())
					note = stackInSlot;
				else
					return false;
			}
			else if(dye.isEmpty())
				dye = stackInSlot;
			else
				return false;
		}
		
		return !note.isEmpty() && !dye.isEmpty();
	}
	
	public ItemStack craft(RecipeInputInventory var1, DynamicRegistryManager var2)
	{
		ItemStack note = ItemStack.EMPTY;
		ItemStack item = ItemStack.EMPTY;
		for(int i=0; i<var1.size(); i++)
		{
			ItemStack stackInSlot = var1.getStack(i);
			if(stackInSlot.getItem() == TFItems.NOTE_ITEM)
			{
				if(note.isEmpty())
					note = stackInSlot;
				else
					return ItemStack.EMPTY;
			}
			else if(item.isEmpty())
				item = stackInSlot;
			else
				return ItemStack.EMPTY;
		}
		
		if(!note.isEmpty() && !item.isEmpty())
		{
			ItemStack theNote = note.copy();
			ItemPrescientNote.addVariable(new WhiteboardObj.Item(item), theNote);
			return theNote;
		}
		return ItemStack.EMPTY;
	}
	
	public boolean fits(int var1, int var2) { return var1 * var2 > 1; }
	
	public ItemStack getOutput(DynamicRegistryManager var1) { return new ItemStack(TFItems.NOTE_ITEM); }
	
	public Identifier getId() { return ID; }
	
	public RecipeSerializer<?> getSerializer() { return TFSpecialRecipes.NOTE_ITEM_SERIALIZER; }
	
	public RecipeType<?> getType() { return TFSpecialRecipes.NOTE_ITEM_TYPE; }
}
