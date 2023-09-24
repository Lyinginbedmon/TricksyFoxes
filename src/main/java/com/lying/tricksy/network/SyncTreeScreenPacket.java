package com.lying.tricksy.network;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.reference.Reference;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class SyncTreeScreenPacket
{
	public static final Identifier PACKET_ID = new Identifier(Reference.ModInfo.MOD_ID, "sync_tree_screen");
	
	public static <T extends PathAwareEntity & ITricksyMob<?>> void send(PlayerEntity player, T tricksy, int syncId)
	{
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeInt(syncId);
		buffer.writeUuid(tricksy.getUuid());
		ServerPlayNetworking.send((ServerPlayerEntity)player, PACKET_ID, buffer);
	}
}
