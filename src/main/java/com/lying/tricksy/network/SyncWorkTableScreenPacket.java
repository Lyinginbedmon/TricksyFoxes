package com.lying.tricksy.network;

import com.lying.tricksy.entity.ITricksyMob;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class SyncWorkTableScreenPacket
{
	public static <T extends PathAwareEntity & ITricksyMob<?>> void send(PlayerEntity player, BlockPos pos, int syncId)
	{
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeInt(syncId);
		buffer.writeBlockPos(pos);
		
		ServerPlayNetworking.send((ServerPlayerEntity)player, TFPacketHandler.SYNC_WORK_TABLE_ID, buffer);
	}
}