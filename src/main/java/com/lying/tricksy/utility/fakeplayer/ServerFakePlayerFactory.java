package com.lying.tricksy.utility.fakeplayer;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public interface ServerFakePlayerFactory
{
	ServerFakePlayerFactory DEFAULT = ServerFakePlayer::new;
	
	public ServerFakePlayer create(FakePlayerBuilder builder, MinecraftServer server, ServerWorld world, GameProfile profile);
}