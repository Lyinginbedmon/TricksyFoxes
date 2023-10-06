package com.lying.tricksy.utility.fakeplayer;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public class FakePlayerBuilder
{
	private final Identifier uniqueID;
	private final ServerFakePlayerFactory factory;
	
	public FakePlayerBuilder(Identifier idIn, ServerFakePlayerFactory factoryIn)
	{
		this.uniqueID = idIn;
		this.factory = factoryIn;
	}
	
	public FakePlayerBuilder(Identifier idIn)
	{
		this(idIn, ServerFakePlayerFactory.DEFAULT);
	}
	
	public Identifier getId() { return this.uniqueID; }
	
	public ServerFakePlayer create(MinecraftServer server, ServerWorld world, GameProfile profile)
	{
		return factory.create(this, server, world, profile);
	}
	
	public ServerFakePlayer create(MinecraftServer server, ServerWorld world, String username)
	{
		return create(server, world, new GameProfile(UUID.randomUUID(), username));
	}
}
