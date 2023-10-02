package com.lying.tricksy.network;

import java.util.UUID;

import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Global;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjBase;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.utility.ServerWhiteboards;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayChannelHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class AddGlobalRefReceiver implements PlayChannelHandler
{
	public static final Identifier PACKET_ID = new Identifier(Reference.ModInfo.MOD_ID, "add_global_ref");
	
	public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
	{
		UUID sageID = buf.readUuid();
		NbtCompound refNbt = buf.readNbt();
		NbtCompound valNbt = buf.readNbt();
		
		WhiteboardRef ref = WhiteboardRef.fromNbt(refNbt);
		IWhiteboardObject<?> val = WhiteboardObjBase.createFromNbt(valNbt);
		System.out.println("Transmitting "+ref.displayName().getString()+" with value "+val.describe().get(0));
		
		ServerWhiteboards boards = ServerWhiteboards.getServerWhiteboards(player.getServer());
		Global whiteboard = boards.getWhiteboardFor(sageID);
		whiteboard.addValue(ref, () -> val);
		boards.markDirty();
	}
}
