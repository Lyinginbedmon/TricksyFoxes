package com.lying.tricksy.item;

import com.lying.tricksy.component.TricksyComponent;
import com.lying.tricksy.init.TFComponents;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class ItemPresciencePeriapt extends Item
{
	public ItemPresciencePeriapt(Settings settings)
	{
		super(settings);
	}
	
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand)
	{
		boolean isClient = user.getWorld().isClient();
		TricksyComponent tracker = TFComponents.TRICKSY_TRACKING.get(entity);
		if(tracker.canBeEnlightened() && !tracker.hasPeriapt())
		{
			tracker.setPeriapt(true);
			if(!isClient && !user.isCreative())
				stack.decrement(1);
			
			return ActionResult.success(isClient);
		}
		
		return ActionResult.PASS;
	}
}
