package com.lying.tricksy.network;

import com.lying.tricksy.entity.ITricksyMob;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class SyncScriptureScreenPacket
{
	public static <T extends PathAwareEntity & ITricksyMob<?>> void send(PlayerEntity player, ItemStack stack, int syncId)
	{
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeInt(syncId);
		buffer.writeItemStack(stack);
		
		ServerPlayNetworking.send((ServerPlayerEntity)player, TFPacketHandler.SYNC_SCRIPTURE_ID, buffer);
	}
}