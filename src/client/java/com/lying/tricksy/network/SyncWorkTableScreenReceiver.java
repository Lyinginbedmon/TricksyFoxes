package com.lying.tricksy.network;

import com.lying.tricksy.screen.WorkTableScreenHandler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class SyncWorkTableScreenReceiver implements ClientPlayNetworking.PlayChannelHandler
{
	public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
	{
		int syncId = buf.readInt();
		BlockPos pos = buf.readBlockPos();
		
		client.execute(() -> 
		{
			if(client.player == null)
				return;
			
			ScreenHandler screenHandler = client.player.currentScreenHandler;
			if(syncId == screenHandler.syncId && screenHandler instanceof WorkTableScreenHandler)
				((WorkTableScreenHandler)screenHandler).setPos(pos);
		});
	}
}