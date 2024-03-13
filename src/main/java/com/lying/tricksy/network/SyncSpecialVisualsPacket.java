package com.lying.tricksy.network;

import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.utility.SpecialVisuals;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class SyncSpecialVisualsPacket
{
	public static <T extends PathAwareEntity & ITricksyMob<?>> void send(PlayerEntity player, SpecialVisuals visuals)
	{
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeNbt(visuals.writeNbt(new NbtCompound()));
		
		ServerPlayNetworking.send((ServerPlayerEntity)player, TFPacketHandler.SYNC_VISUALS_ID, buffer);
	}
}