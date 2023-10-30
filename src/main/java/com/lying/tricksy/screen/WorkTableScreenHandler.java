package com.lying.tricksy.screen;

import java.util.Optional;

import com.lying.tricksy.init.TFScreenHandlerTypes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;

public class WorkTableScreenHandler extends ScreenHandler
{
	private final RecipeInputInventory input = new CraftingInventory(this, 3, 3);
	private final CraftingResultInventory result = new CraftingResultInventory();
	
	public WorkTableScreenHandler(int syncId, PlayerInventory playerInventory)
	{
		super(TFScreenHandlerTypes.WORK_TABLE_SCREEN_HANDLER, syncId);
        this.addSlot(new CraftingResultSlot(playerInventory.player, this.input, this.result, 0, 124, 35));
        for (int i = 0; i < 3; ++i)
            for (int j = 0; j < 3; ++j)
                this.addSlot(new Slot(this.input, j + i * 3, 30 + j * 18, 17 + i * 18));
	}
	
	public void tryCraft(World world)
	{
		if(world.isClient())
			return;
		
		ItemStack itemStack2;
		Optional<CraftingRecipe> optional = world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, input, world);
		if(optional.isPresent() && (itemStack2 = optional.get().craft(input, world.getRegistryManager())).isItemEnabled(world.getEnabledFeatures()))
			setStackInSlot(0, getRevision(), itemStack2);
	}

	@Override
	public ItemStack quickMove(PlayerEntity var1, int var2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canUse(PlayerEntity var1) {
		// TODO Auto-generated method stub
		return false;
	}

}
