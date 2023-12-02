package com.lying.tricksy.utility;

import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.component.Accomplishment;
import com.lying.tricksy.component.TricksyComponent;
import com.lying.tricksy.entity.ai.whiteboard.ConstantsWhiteboard;
import com.lying.tricksy.init.TFAccomplishments;
import com.lying.tricksy.init.TFComponents;
import com.lying.tricksy.init.TFEnlightenmentPaths;
import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.item.ItemPrescientNote;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;

public class ServerBus
{
	public static void registerEventCallbacks()
	{
		ServerLifecycleEvents.SERVER_STARTED.register((server) -> reloadDataPackListeners());
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> reloadDataPackListeners());
		
		ServerEntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD.register((originalEntity, newEntity, origin, destination) -> 
		{
			if(!(newEntity instanceof MobEntity) || !TFEnlightenmentPaths.INSTANCE.isEnlightenable((MobEntity)newEntity))
				return;
			
			TricksyComponent compNew = TFComponents.TRICKSY_TRACKING.get(newEntity);
			compNew.cloneFrom(TFComponents.TRICKSY_TRACKING.get(originalEntity));
			
			if(!compNew.hasAchieved(TFAccomplishments.DIMENSIONAL_TRAVEL))
				compNew.addAccomplishment(TFAccomplishments.DIMENSIONAL_TRAVEL);
			
			RegistryKey<DimensionType> newDim = destination.getDimensionKey();
			compNew.changeFromNether(originalEntity.getBlockPos(), newDim);
			Accomplishment accomplishment = null;
			if(newDim == DimensionTypes.THE_NETHER)
				accomplishment = TFAccomplishments.VISIT_NETHER;
			else if(newDim == DimensionTypes.THE_END)
				accomplishment = TFAccomplishments.VISIT_END;
			else if(newDim == DimensionTypes.OVERWORLD)
				accomplishment = TFAccomplishments.VISIT_OVERWORLD;
			
			if(accomplishment != null)
				compNew.addAccomplishment(accomplishment);
		});
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> 
		{
			// Clear a tricksy mob's prescient candle value when they die
			if(entity instanceof ITricksyMob<?>)
				CandlePowers.getCandlePowers(entity.getServer()).remove(entity.getUuid());
			
			// Identify and award any appropriate accomplishment
			Accomplishment result = null;
			if(entity.getType() == EntityType.ENDER_DRAGON)
				result = TFAccomplishments.SQUIRE;
			else if(entity.getType() == EntityType.WARDEN)
				result = TFAccomplishments.OUTLAW;
			else if(entity.getType() == EntityType.WITHER)
				result = TFAccomplishments.DEATH_DEFIER;
			
			if(result != null)
			{
				Box bounds = entity.getBoundingBox().expand(16, 64, 16);
				int minY = entity.getWorld().getBottomY();
				if(bounds.minY < minY || bounds.maxY < minY)
					bounds = new Box(bounds.minX, Math.max(minY, bounds.minY), bounds.minZ, bounds.maxX, Math.max(minY, bounds.maxY), bounds.maxZ);
				for(MobEntity mob : entity.getWorld().getEntitiesByClass(MobEntity.class, bounds, (mob) -> mob.isAlive() && TFEnlightenmentPaths.INSTANCE.isEnlightenable(mob))) 
				{
					TricksyComponent comp = TFComponents.TRICKSY_TRACKING.get(mob);
					comp.addAccomplishment(result);
				};
			}
		});
		
		/** Prescient Scroll (Entity) handling */
		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> 
		{
			ItemStack heldStack = player.getStackInHand(hand);
			if(heldStack.getItem() == TFItems.NOTE_ENT && player.isSneaking() && entity instanceof LivingEntity)
			{
				if(!world.isClient())
					ItemPrescientNote.Ent.addEntityToStack(heldStack, (LivingEntity)entity);
				return ActionResult.SUCCESS;
			}
			
			return ActionResult.PASS;
		});
	}
	
	private static void reloadDataPackListeners()
	{
		ConstantsWhiteboard.populateTagFilters();
	}
}
