package com.lying.tricksy.entity;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ai.BehaviourTree;
import com.lying.tricksy.entity.ai.Whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.Whiteboard.WorldWhiteboard;
import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.item.ItemSageHat;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

/**
 * Interface defining common features and functions of all Tricksy mobs
 * @author Lying
 */
public interface ITricksyMob
{
	/** Returns true if this mob has a master */
	public default boolean hasMaster() { return getMaster().isPresent(); }
	
	public Optional<UUID> getMaster();
	
	/** Sets the UUID of this mob's master */
	public void setMaster(@Nullable UUID uuidIn);
	
	/**
	 * Returns true if the given entity should be recognised as this mob's master due to a matching Sage Hat
	 * @param living
	 */
	public default boolean isMaster(LivingEntity living)
	{
		if(living.getType() == EntityType.PLAYER && ((PlayerEntity)living).isCreative())
			return true;
		
		if(!hasMaster())
			return false;
		
		for(EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND })
		{
			ItemStack hatStack = living.getEquippedStack(slot);
			if(!hatStack.isEmpty() && hatStack.getItem() == TFItems.SAGE_HAT)
				if(getMaster().get().equals(ItemSageHat.getMasterID(hatStack)))
					return true;
		}
		
		return false;
	}
	
	/** Returns the behaviour tree of this mob.<br>Note: This may not exactly match the structure stored in NBT. */
	public BehaviourTree getBehaviourTree();
	
	/** Returns the local whiteboard of this mob. */
	public LocalWhiteboard getLocalWhiteboard();
	
	public static <T extends PathAwareEntity & ITricksyMob> void updateBehaviourTree(T tricksy)
	{
		if(tricksy.getWorld().isClient())
			return;
		
		// Update whiteboards
		LocalWhiteboard local = tricksy.getLocalWhiteboard();
		WorldWhiteboard global = null;
		
		local.update(tricksy, tricksy.getEntityWorld());
//		master.update(tricksy, tricksy.getEntityWorld());
		
		// Update behaviour tree
		BehaviourTree tree = tricksy.getBehaviourTree();
		
		tree.update(tricksy, local, global);
	}
	
	/** Overwrites the structure of the mob's behaviour tree. */
	public void setBehaviourTree(NbtCompound data);
}
