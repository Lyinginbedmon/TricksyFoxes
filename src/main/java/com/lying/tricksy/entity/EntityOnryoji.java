package com.lying.tricksy.entity;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.BehaviourTree;
import com.lying.tricksy.entity.ai.NodeStatusLog;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.OrderWhiteboard;

import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public class EntityOnryoji extends HostileEntity implements ITricksyMob<EntityOnryoji>
{
	public EntityOnryoji(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ItemStack getStack(int var1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStack(int var1, ItemStack var2) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canPlayerUse(PlayerEntity var1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Optional<UUID> getSage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSage(@Nullable UUID uuidIn) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getColor() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasColor() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public BehaviourTree getBehaviourTree() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeStatusLog getLatestLog() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLatestLog(NodeStatusLog logIn) {
		// TODO Auto-generated method stub

	}

	@Override
	public LocalWhiteboard<EntityOnryoji> getLocalWhiteboard() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GlobalWhiteboard getGlobalWhiteboard() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBehaviourTree(NbtCompound data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void giveCommand(OrderWhiteboard command) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasCustomer() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setCustomer(@Nullable PlayerEntity player) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTreePose(EntityPose pose) {
		// TODO Auto-generated method stub

	}

	@Override
	public EntityPose getTreePose() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Inventory getMainInventory() {
		// TODO Auto-generated method stub
		return null;
	}

}
