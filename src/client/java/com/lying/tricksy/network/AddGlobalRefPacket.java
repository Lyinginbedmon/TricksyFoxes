package com.lying.tricksy.network;

import java.util.UUID;

import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.reference.Reference;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class AddGlobalRefPacket
{
	public static final Identifier PACKET_ID = new Identifier(Reference.ModInfo.MOD_ID, "add_global_ref");
	
	public static void send(PlayerEntity player, UUID sageID, WhiteboardRef ref, IWhiteboardObject<?> val)
	{
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeUuid(sageID);
		buffer.writeNbt(ref.writeToNbt(new NbtCompound()));
		buffer.writeNbt(val.writeToNbt(new NbtCompound()));
		ClientPlayNetworking.send(PACKET_ID, buffer);
	}
}
