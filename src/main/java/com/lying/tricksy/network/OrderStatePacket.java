package com.lying.tricksy.network;

import com.lying.tricksy.api.entity.ITricksyMob;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class OrderStatePacket
{
	public static <T extends PathAwareEntity & ITricksyMob<?>> void start(PlayerEntity player, int color)
	{
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeBoolean(true);
		buf.writeInt(color);
		ServerPlayNetworking.send((ServerPlayerEntity)player, TFPacketHandler.ORDER_STATE_ID, buf);
	}
	
	public static <T extends PathAwareEntity & ITricksyMob<?>> void end(PlayerEntity player)
	{
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeBoolean(false);
		ServerPlayNetworking.send((ServerPlayerEntity)player, TFPacketHandler.ORDER_STATE_ID, buf);
	}
}
