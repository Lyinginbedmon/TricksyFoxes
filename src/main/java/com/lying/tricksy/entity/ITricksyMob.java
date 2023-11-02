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
import com.lying.tricksy.network.SyncInventoryScreenPacket;
import com.lying.tricksy.network.SyncTreeScreenPacket;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.screen.TricksyInventoryScreenHandler;
import com.lying.tricksy.screen.TricksyTreeScreenHandler;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

/**
 * Interface defining common features and functions of all Tricksy mobs
 * @author Lying
 */
public interface ITricksyMob<T extends PathAwareEntity & ITricksyMob<?>> extends InventoryOwner, InventoryChangedListener
{
	public static final EquipmentSlot[] SLOT_ORDER = new EquipmentSlot[] {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND};
	
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
	
	public boolean hasCustomer();
	
	public void setCustomer(@Nullable PlayerEntity player);
	
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
	
	/**	## Inventory methods ##	*/
	
	public static SimpleInventory createInventory() { return new SimpleInventory(6); }
	
	public default void onInventoryChanged(Inventory inv)
	{
		updateEquippedItems(inv);
	}
	
	public default void updateEquippedItems() { updateEquippedItems(getInventory()); }
	
	public default void updateEquippedItems(Inventory inv)
	{
		for(int i=0; i<SLOT_ORDER.length; i++)
		{
			EquipmentSlot slot = SLOT_ORDER[i];
			((PathAwareEntity)this).equipStack(slot, inv.getStack(i));
		}
	}
	
	public default void writeInventory(NbtCompound nbt)
	{
		Inventory inv = getInventory();
		NbtList list = new NbtList();
		for(int i=0; i<inv.size(); i++)
		{
			NbtCompound data = new NbtCompound();
			ItemStack stack = inv.getStack(i);
			if(stack.isEmpty())
				continue;
			
			data.putInt("Slot", i);
			data.put("Item", stack.writeNbt(new NbtCompound()));
			list.add(data);
		}
		nbt.put(INVENTORY_KEY, list);
	}
	
	public default void readInventory(NbtCompound nbt)
	{
		Inventory inv = getInventory();
		if(nbt.contains(INVENTORY_KEY, NbtElement.LIST_TYPE))
		{
			inv.clear();
			NbtList list = nbt.getList(INVENTORY_KEY, NbtElement.COMPOUND_TYPE);
			for(int i=0; i<list.size(); i++)
			{
				NbtCompound data = list.getCompound(i);
				int slot = data.getInt("Slot");
				ItemStack stack = ItemStack.fromNbt(data.getCompound("Item"));
				inv.setStack(slot, stack);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends PathAwareEntity & ITricksyMob<?>> void openTreeScreen(PlayerEntity player, PathAwareEntity tricksy)
	{
		player.openHandledScreen(new SimpleNamedScreenHandlerFactory((id, playerInventory, custom) -> new TricksyTreeScreenHandler(id, playerInventory, (T)tricksy), tricksy.getDisplayName())).ifPresent(syncId -> SyncTreeScreenPacket.send(player, (T)tricksy, syncId));
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends PathAwareEntity & ITricksyMob<?>> void openInventoryScreen(PlayerEntity player, PathAwareEntity tricksy)
	{
		player.openHandledScreen(new SimpleNamedScreenHandlerFactory((id, playerInventory, custom) -> new TricksyInventoryScreenHandler(id, playerInventory, ((T)tricksy).getInventory(), (T)tricksy), tricksy.getDisplayName())).ifPresent(syncId -> SyncInventoryScreenPacket.send(player, (T)tricksy, syncId));
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
