package com.lying.tricksy.network;

import java.util.UUID;

import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBase;
import com.lying.tricksy.utility.ServerWhiteboards;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayChannelHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class AddGlobalRefReceiver implements PlayChannelHandler
{
	public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
	{
		UUID sageID = buf.readUuid();
		NbtCompound refNbt = buf.readNbt();
		NbtCompound valNbt = buf.readNbt();
		
		WhiteboardRef ref = WhiteboardRef.fromNbt(refNbt);
		IWhiteboardObject<?> val = WhiteboardObjBase.createFromNbt(valNbt);
		
		ServerWhiteboards boards = ServerWhiteboards.getServerWhiteboards(player.getServer());
		GlobalWhiteboard whiteboard = boards.getWhiteboardFor(sageID);
		whiteboard.addValue(ref, () -> val);
		boards.markDirty();
		
		RefAddedPacket.send(player);
	}
}
