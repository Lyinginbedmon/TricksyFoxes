package com.lying.tricksy.network;

import com.lying.tricksy.api.entity.ITricksyMob;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class RefAddedPacket
{
	public static <T extends PathAwareEntity & ITricksyMob<?>> void send(PlayerEntity player)
	{
		ServerPlayNetworking.send((ServerPlayerEntity)player, TFPacketHandler.REF_ADDED_ID, new PacketByteBuf(Unpooled.buffer()));
	}
}
