package com.lying.tricksy.entity;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ai.BehaviourTree;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.item.ItemSageHat;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

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
					if(getSage().get().equals(ItemSageHat.getSageID(hatStack)))
						return true;
			}
		
		return false;
	}
	
	/** Returns the behaviour tree of this mob.<br>Note: This may not exactly match the structure stored in NBT, due to runtime value changes. */
	public BehaviourTree getBehaviourTree();
	
	/** Returns the local whiteboard of this mob. */
	public LocalWhiteboard<T> getLocalWhiteboard();
	
	public GlobalWhiteboard getGlobalWhiteboard();
	
	@SuppressWarnings("unchecked")
	public static <T extends PathAwareEntity & ITricksyMob<?>> void updateBehaviourTree(T tricksy)
	{
		if(tricksy.getWorld().isClient())
			return;
		
		// Update whiteboards
		LocalWhiteboard<T> local = (LocalWhiteboard<T>)tricksy.getLocalWhiteboard();
		GlobalWhiteboard global = tricksy.getGlobalWhiteboard();
		
		// Update local whiteboard
		local.tick();
		
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
	
	public default void bark(Bark bark) { }
	
	public default Bark currentBark() { return Bark.NONE; }
	
	public void setSleeping(boolean var);
	
	public boolean isSleeping();
	
	public ItemStack getProjectileType(ItemStack stack);
	
	/** Performs the same projectile-fetching as used by Piglins and Pillagers */
	public static ItemStack getRangedProjectile(ItemStack stack, LivingEntity shooter)
	{
        if(stack.getItem() instanceof RangedWeaponItem)
        {
            Predicate<ItemStack> predicate = ((RangedWeaponItem)stack.getItem()).getHeldProjectiles();
            ItemStack ammo = RangedWeaponItem.getHeldProjectile(shooter, predicate);
            return ammo.isEmpty() ? new ItemStack(Items.ARROW) : ammo;
        }
        return ItemStack.EMPTY;
	}
	
	public static enum Bark implements StringIdentifiable
	{
		NONE,
		HAPPY,
		CURIOUS,
		CONFUSED,
		ALERT;
		
		private final Identifier texture;
		
		private Bark()
		{
			this.texture = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/bark/"+asString()+".png");
		}
		
		public String asString() { return name().toLowerCase(); }
		
		public Identifier textureLocation() { return this.texture; }
	}
}
