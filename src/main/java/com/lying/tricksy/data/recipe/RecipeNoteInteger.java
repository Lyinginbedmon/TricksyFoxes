package com.lying.tricksy.data.recipe;

import com.lying.tricksy.data.TFItemTags;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
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

public class RecipeNoteInteger extends RecipeNote
{
	public static final Identifier ID = new Identifier(Reference.ModInfo.MOD_ID, "note_integer");
	
	public RecipeNoteInteger() { super(ID); }
	
	public boolean matches(RecipeInputInventory var1, World var2)
	{
		ItemStack note = ItemStack.EMPTY;
		for(int i=0; i<var1.size(); i++)
		{
			ItemStack stackInSlot = var1.getStack(i);
			if(stackInSlot.isEmpty())
				continue;
			else if(stackInSlot.getItem() == TFItems.NOTE_INT && !ISealableItem.isSealed(stackInSlot))
			{
				if(note.isEmpty())
					note = stackInSlot.copy();
				else
					return false;
			}
			else if(!stackInSlot.isIn(TFItemTags.PAPER))
				return false;
		}
		return !note.isEmpty();
	}
	
	public ItemStack craft(RecipeInputInventory var1, DynamicRegistryManager var2)
	{
		ItemStack note = ItemStack.EMPTY;
		int paperTally = 0;
		for(int i=0; i<var1.size(); i++)
		{
			ItemStack stackInSlot = var1.getStack(i);
			if(stackInSlot.isEmpty())
				continue;
			else if(stackInSlot.getItem() == TFItems.NOTE_INT && !ISealableItem.isSealed(stackInSlot))
			{
				if(note.isEmpty())
					note = stackInSlot.copy();
				else
					return ItemStack.EMPTY;
			}
			else if(stackInSlot.isIn(TFItemTags.PAPER))
				paperTally += stackInSlot.getCount();
			else
				return ItemStack.EMPTY;
		}
		
		if(!note.isEmpty() && paperTally >= 0)
		{
			ItemStack theNote = note.copy();
			ItemPrescientNote.addVariable(new WhiteboardObj.Int(paperTally), theNote);
			return theNote;
		}
		return ItemStack.EMPTY;
	}
	
	public ItemStack getOutput(DynamicRegistryManager var1) { return new ItemStack(TFItems.NOTE_INT); }
	
	public Identifier getId() { return ID; }
	
	public RecipeSerializer<?> getSerializer() { return TFSpecialRecipes.NOTE_INTEGER_SERIALIZER; }
}
