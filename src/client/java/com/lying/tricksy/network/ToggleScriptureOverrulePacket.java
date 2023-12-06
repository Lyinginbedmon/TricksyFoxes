package com.lying.tricksy.network;

import com.lying.tricksy.api.entity.ITricksyMob;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

public class ToggleScriptureOverrulePacket
{
	public static <T extends PathAwareEntity & ITricksyMob<?>> void send(PlayerEntity player, boolean bool)
	{
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeBoolean(bool);
		ClientPlayNetworking.send(TFPacketHandler.TOGGLE_SCRIPTURE_ID, buffer);
	}
}
