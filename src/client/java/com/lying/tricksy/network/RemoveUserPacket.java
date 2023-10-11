package com.lying.tricksy.network;

import java.util.UUID;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

public class RemoveUserPacket
{
	public static void send(PlayerEntity player, UUID tricksyID)
	{
		if(tricksyID == null)
			return;
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeUuid(tricksyID);
		ClientPlayNetworking.send(TFPacketHandler.REMOVE_USER_ID, buffer);
	}
}
