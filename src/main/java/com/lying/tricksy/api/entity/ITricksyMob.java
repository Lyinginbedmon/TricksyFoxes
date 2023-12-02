package com.lying.tricksy.api.entity;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ai.BehaviourTree;
import com.lying.tricksy.entity.ai.NodeStatusLog;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.item.ItemSageHat;
import com.lying.tricksy.network.SyncInventoryScreenPacket;
import com.lying.tricksy.network.SyncTreeScreenPacket;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.screen.TricksyInventoryScreenHandler;
import com.lying.tricksy.screen.TricksyTreeScreenHandler;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

/**
 * Interface defining common features and functions of all Tricksy mobs
 * @author Lying
 */
public interface ITricksyMob<T extends PathAwareEntity & ITricksyMob<?>> extends Inventory, InventoryChangedListener
{
	public static final Map<EquipmentSlot, Integer> SLOT_TO_INDEX_MAP = Map.of(
			EquipmentSlot.FEET, 0,
			EquipmentSlot.LEGS, 1,
			EquipmentSlot.CHEST, 2,
			EquipmentSlot.HEAD, 3,
			EquipmentSlot.MAINHAND, 4,
			EquipmentSlot.OFFHAND, 5);
	public static final Map<Integer, EquipmentSlot> INDEX_TO_SLOT_MAP = Map.of(
			0, EquipmentSlot.FEET,
			1, EquipmentSlot.LEGS, 
			2, EquipmentSlot.CHEST,
			3, EquipmentSlot.HEAD,
			4, EquipmentSlot.MAINHAND,
			5, EquipmentSlot.OFFHAND);
	
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
	
	public int getColor();
	
	/** Returns the behaviour tree of this mob.<br>Note: This may not exactly match the structure stored in NBT, due to runtime value changes. */
	public BehaviourTree getBehaviourTree();
	
	/** Returns the latest activity log of the mob's behaviour tree */
	public NodeStatusLog getLatestLog();
	
	/** Stores the latest activity log in the mob's data manager */
	public void setLatestLog(NodeStatusLog logIn);
	
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
		if(tree == null)
			return;
		
		tree.update(tricksy, local, global);
		tricksy.setLatestLog(tree.latestLog());
	}
	
	/** Overwrites the structure of the mob's behaviour tree. */
	public void setBehaviourTree(NbtCompound data);
	
	public boolean hasCustomer();
	
	public void setCustomer(@Nullable PlayerEntity player);
	
	public default void bark(Bark bark) { }
	
	public default Bark currentBark() { return Bark.NONE; }
	
	public default void playSoundForBark(@NotNull Bark bark) { }
	
	public void setTreeSleeping(boolean var);
	
	public boolean isTreeSleeping();
	
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
	
	/*	## Inventory methods ##	*/
	
	public default void clear()
	{
		for(int i=0; i<size(); i++)
			setStack(i, ItemStack.EMPTY);
	}
	
	public default int size() { return EquipmentSlot.values().length; }
	
	public default boolean isEmpty()
	{
		for(int i=0; i<size(); i++)
			if(!getStack(i).isEmpty())
				return false;
		return true;
	}
	
	public default ItemStack removeStack(int slot, int amount)
	{
		if(slot < 0 || slot >= size() || getStack(slot).isEmpty() || amount <= 0)
			return ItemStack.EMPTY;
		ItemStack stack = getStack(slot).split(amount);
		if(!stack.isEmpty())
			markDirty();
		return stack;
	}
	
	public default ItemStack removeStack(int var1)
	{
		ItemStack stack = getStack(var1);
		if(stack.isEmpty())
			return ItemStack.EMPTY;
		setStack(var1, ItemStack.EMPTY);
		return stack;
	}
	
	public default void onInventoryChanged(Inventory inv)
	{
		updateEquippedItems(inv);
	}
	
	public default void updateEquippedItems(Inventory inv)
	{
		for(int i=0; i<INDEX_TO_SLOT_MAP.size(); i++)
		{
			EquipmentSlot slot = INDEX_TO_SLOT_MAP.get(i);
			((PathAwareEntity)this).equipStack(slot, inv.getStack(i));
		}
	}
	
	public default void markDirty() { }
	
	public Inventory getMainInventory();
	
	@SuppressWarnings("unchecked")
	public static <T extends PathAwareEntity & ITricksyMob<?>> void openTreeScreen(PlayerEntity player, PathAwareEntity tricksy)
	{
		player.openHandledScreen(new SimpleNamedScreenHandlerFactory((id, playerInventory, custom) -> new TricksyTreeScreenHandler(id, playerInventory, (T)tricksy), tricksy.getDisplayName())).ifPresent(syncId -> SyncTreeScreenPacket.send(player, (T)tricksy, syncId));
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends PathAwareEntity & ITricksyMob<?>> void openInventoryScreen(PlayerEntity player, PathAwareEntity tricksy)
	{
		player.openHandledScreen(new SimpleNamedScreenHandlerFactory((id, playerInventory, custom) -> new TricksyInventoryScreenHandler(id, playerInventory, ((T)tricksy).getMainInventory(), (T)tricksy), tricksy.getDisplayName())).ifPresent(syncId -> SyncInventoryScreenPacket.send(player, (T)tricksy, syncId));
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
