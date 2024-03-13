package com.lying.tricksy.utility;

import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.component.Accomplishment;
import com.lying.tricksy.component.TricksyComponent;
import com.lying.tricksy.entity.ai.BehaviourTree;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.whiteboard.ConstantsWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardManager;
import com.lying.tricksy.init.TFAccomplishments;
import com.lying.tricksy.init.TFComponents;
import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.item.ItemPrescientNote;
import com.lying.tricksy.network.SyncSpecialVisualsPacket;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;

public class ServerBus
{
	public static final Event<AfterDamage> AFTER_DAMAGE = EventFactory.createArrayBacked(AfterDamage.class, callbacks -> (entity, source, amount) -> 
	{
		for(AfterDamage callback : callbacks)
			callback.afterDamage(entity, source, amount);
	});
	
	@FunctionalInterface
	public interface AfterDamage
	{
		void afterDamage(LivingEntity entity, DamageSource source, float amount);
	}
	
	public static void registerEventCallbacks()
	{
		ServerLifecycleEvents.SERVER_STARTED.register((server) -> reloadDataPackListeners());
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> reloadDataPackListeners());
		
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> { if(entity.getType() == EntityType.PLAYER) SyncSpecialVisualsPacket.send((PlayerEntity)entity, SpecialVisuals.getVisuals(world)); });
		
		ServerEntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD.register((originalEntity, newEntity, origin, destination) -> 
		{
			TricksyComponent compOld;
			if(!(newEntity instanceof MobEntity))
				return;
			else if(!(compOld = TFComponents.TRICKSY_TRACKING.get(originalEntity)).shouldTrackAccomplishments())
				return;
			
			TricksyComponent compNew = TFComponents.TRICKSY_TRACKING.get(newEntity);
			compNew.cloneFrom(compOld);
			
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
			{
				CandlePowers.getCandlePowers(entity.getServer()).remove(entity.getUuid());
				BehaviourForest.getForest(entity.getServer()).remove(entity.getUuid());
			}
			
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
				
				for(MobEntity mob : entity.getWorld().getEntitiesByClass(MobEntity.class, bounds, (mob) -> mob.isAlive() && TFComponents.TRICKSY_TRACKING.get(mob).shouldTrackAccomplishments()))
					TFComponents.TRICKSY_TRACKING.get(mob).addAccomplishment(result);
			}
		});
		
		/** Prescient Scroll (Entity) handling */
		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> 
		{
			ItemStack heldStack = player.getStackInHand(hand);
			if(heldStack.getItem() == TFItems.NOTE_ENT && player.isSneaking() && entity instanceof LivingEntity)
			{
				if(!world.isClient())
				{
					ItemPrescientNote.Ent.addEntityToStack(heldStack, (LivingEntity)entity);
					player.setStackInHand(hand, heldStack);
				}
				return ActionResult.SUCCESS;
			}
			
			return ActionResult.PASS;
		});
		
		ServerBus.AFTER_DAMAGE.register((entity, damage, amount) -> 
		{
			if(!(entity instanceof ITricksyMob<?>) || !(entity instanceof PathAwareEntity))
				return;
			BehaviourTree tree = ((ITricksyMob<?>)entity).getBehaviourTree();
			if(tree.isRunning())
				findActiveAndBreak(tree.root(), (PathAwareEntity & ITricksyMob<?>)entity);
		});
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends PathAwareEntity & ITricksyMob<?>> void findActiveAndBreak(TreeNode<?> nodeIn, T tricksy)
	{
		if(!nodeIn.isRunning())
			return;
		else if(nodeIn.getSubType().breaksOnDamage())
			nodeIn.stop(tricksy, (WhiteboardManager<T>)tricksy.getWhiteboards());
		else if(nodeIn.hasChildren())
			nodeIn.children().forEach(child -> findActiveAndBreak(child, tricksy));
	}
	
	private static void reloadDataPackListeners()
	{
		ConstantsWhiteboard.populateTagFilters();
	}
}
