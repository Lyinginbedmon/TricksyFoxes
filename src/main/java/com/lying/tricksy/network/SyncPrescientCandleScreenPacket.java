package com.lying.tricksy.network;

import java.util.UUID;

import com.lying.tricksy.entity.ITricksyMob;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class SyncPrescientCandleScreenPacket
{
	public static <T extends PathAwareEntity & ITricksyMob<?>> void send(PlayerEntity player, UUID idIn, int syncId)
	{
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeInt(syncId);
		buffer.writeUuid(idIn);
		
		ServerPlayNetworking.send((ServerPlayerEntity)player, TFPacketHandler.SYNC_PRESCIENT_CANDLE_ID, buffer);
	}
}