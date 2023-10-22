package com.lying.tricksy.data.recipe;

import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.init.TFSpecialRecipes;
import com.lying.tricksy.item.ISealableItem;
import com.lying.tricksy.item.ItemPrescientNote;
import com.lying.tricksy.reference.Reference;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class RecipeNoteRegion2 extends RecipeNote
{
	public static final Identifier ID = new Identifier(Reference.ModInfo.MOD_ID, "note_region");
	
	public RecipeNoteRegion2() { super(ID); }
	
	public boolean matches(RecipeInputInventory var1, World var2)
	{
		ItemStack note1 = ItemStack.EMPTY;
		ItemStack note2 = ItemStack.EMPTY;
		for(int i=0; i<var1.size(); i++)
		{
			ItemStack stackInSlot = var1.getStack(i);
			if(stackInSlot.isEmpty())
				continue;
			else if(stackInSlot.getItem() == TFItems.NOTE_REG && !ISealableItem.isSealed(stackInSlot))
			{
				if(note1.isEmpty())
					note1 = stackInSlot;
				else if(note2.isEmpty())
					note2 = stackInSlot;
				else
					return false;
			}
			else
				return false;
		}
		
		return !note1.isEmpty() && !note2.isEmpty();
	}
	
	public ItemStack craft(RecipeInputInventory var1, DynamicRegistryManager var2)
	{
		ItemStack note1 = ItemStack.EMPTY;
		ItemStack note2 = ItemStack.EMPTY;
		for(int i=0; i<var1.size(); i++)
		{
			ItemStack stackInSlot = var1.getStack(i);
			if(stackInSlot.isEmpty())
				continue;
			else if(stackInSlot.getItem() == TFItems.NOTE_REG && !ISealableItem.isSealed(stackInSlot) && ItemPrescientNote.getVariable(stackInSlot).size() > 0)
			{
				if(note1.isEmpty())
					note1 = stackInSlot;
				else if(note2.isEmpty())
					note2 = stackInSlot;
				else
					return ItemStack.EMPTY;
			}
			else
				return ItemStack.EMPTY;
		}
		
		if(!note1.isEmpty() && !note2.isEmpty())
		{
			ItemStack theNote = note1.copy();
			ItemPrescientNote.addVariable(ItemPrescientNote.getVariable(note2), note1);
			return theNote;
		}
		return ItemStack.EMPTY;
	}
	
	public ItemStack getOutput(DynamicRegistryManager var1) { return new ItemStack(TFItems.NOTE_REG); }
	
	public Identifier getId() { return ID; }
	
	public RecipeSerializer<?> getSerializer() { return TFSpecialRecipes.NOTE_REGION_SERIALIZER; }
}
