package com.lying.tricksy.data.recipe;

import com.lying.tricksy.data.TFItemTags;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObj;
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

public class RecipeNoteBool extends RecipeNote
{
	public static final Identifier ID = new Identifier(Reference.ModInfo.MOD_ID, "note_boolean");
	
	public RecipeNoteBool() { super(ID); }
	
	public boolean matches(RecipeInputInventory var1, World var2)
	{
		ItemStack note = ItemStack.EMPTY;
		ItemStack dye = ItemStack.EMPTY;
		for(int i=0; i<var1.size(); i++)
		{
			ItemStack stackInSlot = var1.getStack(i);
			if(stackInSlot.isEmpty())
				continue;
			else if(stackInSlot.getItem() == TFItems.NOTE_BOOL && !ISealableItem.isSealed(stackInSlot))
			{
				if(note.isEmpty())
					note = stackInSlot;
				else
					return false;
			}
			else if(stackInSlot.isIn(TFItemTags.DYE_BLACK) || stackInSlot.isIn(TFItemTags.DYE_WHITE))
			{
				if(dye.isEmpty())
					dye = stackInSlot;
				else
					return false;
			}
			else
				return false;
		}
		
		return !note.isEmpty() && !dye.isEmpty();
	}
	
	public ItemStack craft(RecipeInputInventory var1, DynamicRegistryManager var2)
	{
		ItemStack note = ItemStack.EMPTY;
		ItemStack dye = ItemStack.EMPTY;
		for(int i=0; i<var1.size(); i++)
		{
			ItemStack stackInSlot = var1.getStack(i);
			if(stackInSlot.isEmpty())
				continue;
			else if(stackInSlot.getItem() == TFItems.NOTE_BOOL && !ISealableItem.isSealed(stackInSlot))
			{
				if(note.isEmpty())
					note = stackInSlot;
				else
					return ItemStack.EMPTY;
			}
			else if(stackInSlot.isIn(TFItemTags.DYE_BLACK) || stackInSlot.isIn(TFItemTags.DYE_WHITE))
			{
				if(dye.isEmpty())
					dye = stackInSlot;
				else
					return ItemStack.EMPTY;
			}
			else
				return ItemStack.EMPTY;
		}
		
		if(!note.isEmpty() && !dye.isEmpty())
		{
			ItemStack theNote = note.copy();
			ItemPrescientNote.addVariable(new WhiteboardObj.Bool(dye.isIn(TFItemTags.DYE_WHITE)), theNote);
			return theNote;
		}
		return ItemStack.EMPTY;
	}
	
	public ItemStack getOutput(DynamicRegistryManager var1) { return new ItemStack(TFItems.NOTE_BOOL); }
	
	public Identifier getId() { return ID; }
	
	public RecipeSerializer<?> getSerializer() { return TFSpecialRecipes.NOTE_BOOLEAN_SERIALIZER; }
}
