package com.lying.tricksy.utility.fakeplayer;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public class ServerFakePlayer extends ServerPlayerEntity
{
	private final FakePlayerBuilder builder;
	
	protected ServerFakePlayer(FakePlayerBuilder builderIn, MinecraftServer server, ServerWorld world, GameProfile profile)
	{
		super(server, world, profile);
		this.builder = builderIn;
		this.networkHandler = new ServerFakePlayNetworkHandler(server, FakeClientConnection.SERVER_FAKE_CONNECTION, this);
	}
	
	public FakePlayerBuilder getBuilder() { return this.builder; }
	
	@Override
	public void tick() { }
	
	@Override
	public boolean canTakeDamage() { return false; }
	
	public static ServerFakePlayer makeForMob(PathAwareEntity entity, Identifier builderID)
	{
		FakePlayerBuilder builder = new FakePlayerBuilder(builderID);
		ServerFakePlayer player = builder.create(entity.getServer(), (ServerWorld)entity.getWorld(), entity.getName().getString());
		
		player.copyPositionAndRotation(entity);
		
		player.setCurrentHand(entity.getActiveHand());
		player.setStackInHand(Hand.MAIN_HAND, entity.getMainHandStack().copy());
		player.setStackInHand(Hand.OFF_HAND, entity.getOffHandStack().copy());
		for(EquipmentSlot slot : EquipmentSlot.values())
			player.equipStack(slot, entity.getEquippedStack(slot).copy());
		
		return player;
	}
}
