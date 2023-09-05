package com.lying.tricksy.entity;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.init.TFEntityTypes;
import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.item.ItemSageHat;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class EntityTricksyFox extends AnimalEntity
{
	public static final TrackedData<Optional<UUID>> OWNER_UUID = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
	public static final TrackedData<NbtCompound> TREE_NBT = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
	public static final TrackedData<NbtCompound> WHITEBOARD_NBT = DataTracker.registerData(EntityTricksyFox.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
	
	public EntityTricksyFox(EntityType<? extends AnimalEntity> entityType, World world)
	{
		super(TFEntityTypes.TRICKSY_FOX, world);
	}
	
	public void initDataTracker()
	{
		super.initDataTracker();
		this.getDataTracker().startTracking(OWNER_UUID, Optional.empty());
	}
	
	public void readCustomDataFromNbt(NbtCompound data)
	{
		super.readCustomDataFromNbt(data);
		if(data.contains("MasterID"))
			setMaster(data.getUuid("MasterID"));
	}
	
	public void writeCustomDataToNbt(NbtCompound data)
	{
		super.writeCustomDataToNbt(data);
		if(hasMaster())
			data.putUuid("MasterID", getMaster().get());
	}
	
	@SuppressWarnings("resource")
	public ActionResult interactMob(PlayerEntity player, Hand hand)
	{
		boolean isClient = getEntityWorld().isClient;
		ItemStack heldStack = player.getStackInHand(hand);
		if(heldStack.getItem() == TFItems.SAGE_HAT && !hasMaster())
		{
			setMaster(ItemSageHat.getMasterID(heldStack, player, true));
			return ActionResult.success(isClient);
		}
		
		return super.interactMob(player, hand);
	}
	
	@Nullable
	public PassiveEntity createChild(ServerWorld arg0, PassiveEntity arg1)
	{
		if(arg1.getType() == this.getType())
			return new FoxEntity(EntityType.FOX, arg0);
		
		return null;
	}
	
	public boolean hasMaster() { return getMaster().isPresent(); }
	
	public Optional<UUID> getMaster() { return this.getDataTracker().get(OWNER_UUID); }
	
	/**
	 * Returns true if the given entity should be recognised as this fox's master due to a matching Sage Hat
	 * @param living
	 */
	public boolean isMaster(LivingEntity living)
	{
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
	
	public void setMaster(UUID uuidIn) { this.getDataTracker().set(OWNER_UUID, Optional.of(uuidIn)); }
}
