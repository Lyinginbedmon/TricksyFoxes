package com.lying.tricksy.data.recipe;

import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjEntity;
import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.init.TFSpecialRecipes;
import com.lying.tricksy.item.ISealableItem;
import com.lying.tricksy.item.ItemPrescientNote;
import com.lying.tricksy.reference.Reference;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class RecipeNoteEntity extends RecipeNote
{
	public static final Identifier ID = new Identifier(Reference.ModInfo.MOD_ID, "note_entity");
	
	public RecipeNoteEntity() { super(ID); }
	
	public boolean matches(RecipeInputInventory var1, World var2)
	{
		ItemStack note = ItemStack.EMPTY;
		ItemStack tripwire = ItemStack.EMPTY;
		for(int i=0; i<var1.size(); i++)
		{
			ItemStack stackInSlot = var1.getStack(i);
			if(stackInSlot.isEmpty())
				continue;
			else if(stackInSlot.getItem() == TFItems.NOTE_ENT && !ItemPrescientNote.Ent.isFilterList(stackInSlot) && !ISealableItem.isSealed(stackInSlot))
			{
				if(note.isEmpty())
					note = stackInSlot;
				else
					return false;
			}
			else if(stackInSlot.isOf(Items.TRIPWIRE_HOOK))
			{
				if(tripwire.isEmpty())
					tripwire = stackInSlot;
				else
					return false;
			}
			else
				return false;
		}
		
		return !note.isEmpty() && !tripwire.isEmpty();
	}
	
	public ItemStack craft(RecipeInputInventory var1, DynamicRegistryManager var2)
	{
		ItemStack note = ItemStack.EMPTY;
		ItemStack tripwire = ItemStack.EMPTY;
		for(int i=0; i<var1.size(); i++)
		{
			ItemStack stackInSlot = var1.getStack(i);
			if(stackInSlot.isEmpty())
				continue;
			else if(stackInSlot.getItem() == TFItems.NOTE_ENT && !ISealableItem.isSealed(stackInSlot) && !ItemPrescientNote.Ent.isFilterList(stackInSlot))
			{
				if(note.isEmpty())
					note = stackInSlot;
				else
					return ItemStack.EMPTY;
			}
			else if(stackInSlot.isOf(Items.TRIPWIRE_HOOK))
			{
				if(tripwire.isEmpty())
					tripwire = stackInSlot;
				else
					return ItemStack.EMPTY;
			}
			else
				return ItemStack.EMPTY;
		}
		
		if(!note.isEmpty() && !tripwire.isEmpty())
		{
			ItemStack theNote = note.copy();
			WhiteboardObjEntity value = (WhiteboardObjEntity)ItemPrescientNote.getVariable(note).as(TFObjType.ENT);
			value.setFilter(true);
			ItemPrescientNote.setVariable(value, theNote);
			return theNote;
		}
		return ItemStack.EMPTY;
	}
	
	public ItemStack getOutput(DynamicRegistryManager var1) { return new ItemStack(TFItems.NOTE_ENT); }
	
	public Identifier getId() { return ID; }
	
	public RecipeSerializer<?> getSerializer() { return TFSpecialRecipes.NOTE_ENTITY_SERIALIZER; }
}
