package com.lying.tricksy.network;

import com.lying.tricksy.entity.ITricksyMob;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class SyncInventoryScreenPacket
{
	public static <T extends PathAwareEntity & ITricksyMob<?>> void send(PlayerEntity player, T tricksy, int syncId)
	{
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeInt(syncId);
		buffer.writeUuid(tricksy.getUuid());
		
		ServerPlayNetworking.send((ServerPlayerEntity)player, TFPacketHandler.SYNC_INVENTORY_ID, buffer);
	}
}