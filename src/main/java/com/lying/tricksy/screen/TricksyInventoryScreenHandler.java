package com.lying.tricksy.screen;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.init.TFScreenHandlerTypes;
import com.mojang.datafixers.util.Pair;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.EquipmentSlot.Type;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

public class TricksyInventoryScreenHandler extends ScreenHandler implements ITricksySyncable
{
	public static final Identifier BLOCK_ATLAS_TEXTURE = PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
	public static final Identifier[] EMPTY_ARMOR_SLOT_TEXTURES = new Identifier[] {PlayerScreenHandler.EMPTY_BOOTS_SLOT_TEXTURE, PlayerScreenHandler.EMPTY_LEGGINGS_SLOT_TEXTURE, PlayerScreenHandler.EMPTY_CHESTPLATE_SLOT_TEXTURE, PlayerScreenHandler.EMPTY_HELMET_SLOT_TEXTURE};
	private final SimpleInventory inventory;
	
	private PathAwareEntity tricksyMob;
	private ITricksyMob<?> tricksy;
	private UUID tricksyID;
	
	public <T extends PathAwareEntity & ITricksyMob<?>> TricksyInventoryScreenHandler(int syncId, PlayerInventory playerInventory, SimpleInventory inventory, final T entity)
	{
		super(TFScreenHandlerTypes.INVENTORY_SCREEN_HANDLER, syncId);
		this.tricksyMob = entity;
		this.tricksy = entity;
		this.inventory = inventory;
		
		// Tricksy inventory slots
		for(int i=0; i<4; ++i)
		{
			final EquipmentSlot slot = EquipmentSlot.fromTypeIndex(Type.ARMOR, i);
			this.addSlot(new Slot(inventory, i, 48, 8 + (54 - i * 18))
			{
				public int getMaxItemCount() { return 1; }
				
				public boolean canInsert(ItemStack stack) { return slot == MobEntity.getPreferredEquipmentSlot(stack); }
				
				public boolean canTakeItems(PlayerEntity player)
				{
					ItemStack stack = getStack();
					if(!stack.isEmpty() && !player.isCreative() && EnchantmentHelper.hasBindingCurse(stack))
						return false;
					return super.canTakeItems(player);
				}
				
				public Pair<Identifier, Identifier> getBackgroundSprite() { return Pair.of(BLOCK_ATLAS_TEXTURE, EMPTY_ARMOR_SLOT_TEXTURES[slot.getEntitySlotId()]); }
			});
		}
		this.addSlot(new Slot(inventory, 4, 66, 8 + 18));
		this.addSlot(new Slot(inventory, 5, 30, 8 + 18)
		{
			public Pair<Identifier, Identifier> getBackgroundSprite() { return Pair.of(BLOCK_ATLAS_TEXTURE, PlayerScreenHandler.EMPTY_OFFHAND_ARMOR_SLOT); }
		});
		
		// Player inventory slots
		for(int k=0; k<3; ++k)
			for(int l=0; l<9; ++l)
				this.addSlot(new Slot(playerInventory, l + k * 9 + 9, 8 + l * 18, 110 + (k - 1) * 18));
		
		for(int k=0; k<9; ++k)
			this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 150));
	}
	
	public void sync(ITricksyMob<?> tricksyIn, PathAwareEntity mobIn)
	{
		this.tricksy = tricksyIn;
		this.tricksyMob = mobIn;
	}
	
	public void loadInventory(NbtCompound nbt)
	{
		if(nbt.contains(InventoryOwner.INVENTORY_KEY, NbtElement.LIST_TYPE))
			this.inventory.readNbtList(nbt.getList(InventoryOwner.INVENTORY_KEY, NbtElement.COMPOUND_TYPE));
	}
	
	public UUID tricksyUUID() { return this.tricksyID; }
	
	public void setUUID(UUID idIn) { this.tricksyID = idIn; }
	
	@Nullable
	public PathAwareEntity getTricksyMob() { return this.tricksyMob; }
	
	public ItemStack quickMove(PlayerEntity player, int slot)
	{
		ItemStack result = ItemStack.EMPTY;
		Slot targetSlot = (Slot)this.slots.get(slot);
		if (targetSlot != null && targetSlot.hasStack()) {
			ItemStack stackInSlot = targetSlot.getStack();
			result = stackInSlot.copy();
			int i = this.inventory.size();
			if(slot < i)
			{
				if (!this.insertItem(stackInSlot, i, this.slots.size(), true))
					return ItemStack.EMPTY;
			}
			else if (this.getSlot(1).canInsert(stackInSlot) && !this.getSlot(1).hasStack())
			{
				if (!this.insertItem(stackInSlot, 1, 2, false))
					return ItemStack.EMPTY;
			}
			else if (this.getSlot(0).canInsert(stackInSlot))
			{
				if (!this.insertItem(stackInSlot, 0, 1, false))
					return ItemStack.EMPTY;
			}
			else if (i <= 2 || !this.insertItem(stackInSlot, 2, i, false))
			{
				int k;
				int j = i;
				int l = k = j + 27;
				int m = l + 9;
				if (slot >= l && slot < m ? !this.insertItem(stackInSlot, j, k, false) : (slot >= j && slot < k ? !this.insertItem(stackInSlot, l, m, false) : !this.insertItem(stackInSlot, l, k, false)))
					return ItemStack.EMPTY;
				return ItemStack.EMPTY;
			}
			if(stackInSlot.isEmpty())
				targetSlot.setStack(ItemStack.EMPTY);
			else
				targetSlot.markDirty();
		}
		return result;
	}
	
	public boolean canUse(PlayerEntity player) { return tricksy != null && tricksyMob.isAlive() && tricksy.isSage(player) && tricksyMob.distanceTo(player) < 6D; }
}
