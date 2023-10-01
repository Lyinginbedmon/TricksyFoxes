package com.lying.tricksy.entity;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ai.BehaviourTree;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Local;
import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.item.ItemSageHat;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

/**
 * Interface defining common features and functions of all Tricksy mobs
 * @author Lying
 */
public interface ITricksyMob<T extends PathAwareEntity & ITricksyMob<?>>
{
	/** Returns true if this mob has a sage */
	public default boolean hasSage() { return getSage().isPresent(); }
	
	public Optional<UUID> getSage();
	
	/** Sets the UUID of this mob's sage */
	public void setSage(@Nullable UUID uuidIn);
	
	/**
	 * Returns true if the given entity should be recognised as this mob's sage due to a matching Sage Hat
	 * @param living
	 */
	public default boolean isSage(LivingEntity living)
	{
		if(!hasSage())
			return false;
		
		if(living.getType() == EntityType.PLAYER && ((PlayerEntity)living).isCreative())
			return true;
		else
			for(EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND })
			{
				ItemStack hatStack = living.getEquippedStack(slot);
				if(!hatStack.isEmpty() && hatStack.getItem() == TFItems.SAGE_HAT)
					if(getSage().get().equals(ItemSageHat.getMasterID(hatStack)))
						return true;
			}
		
		return false;
	}
	
	/** Returns the behaviour tree of this mob.<br>Note: This may not exactly match the structure stored in NBT, due to runtime value changes. */
	public BehaviourTree getBehaviourTree();
	
	/** Returns the local whiteboard of this mob. */
	public Whiteboard.Local<T> getLocalWhiteboard();
	
	public Whiteboard.Global getGlobalWhiteboard();
	
	@SuppressWarnings("unchecked")
	public static <T extends PathAwareEntity & ITricksyMob<?>> void updateBehaviourTree(T tricksy)
	{
		if(tricksy.getWorld().isClient())
			return;
		
		// Update whiteboards
		Whiteboard.Local<T> local = (Local<T>)tricksy.getLocalWhiteboard();
		Whiteboard.Global global = tricksy.getGlobalWhiteboard();
		
		// Update behaviour tree
		BehaviourTree tree = tricksy.getBehaviourTree();
		
		tree.update(tricksy, local, global);
	}
	
	/** Overwrites the structure of the mob's behaviour tree. */
	public void setBehaviourTree(NbtCompound data);
	
	public int activeUsers();
	
	public void addUser();
	
	public void removeUser();
	
	public void logStatus(Text message);
	
	public Text latestLog();
	
	public void setSleeping(boolean var);
	
	public boolean isSleeping();
}
