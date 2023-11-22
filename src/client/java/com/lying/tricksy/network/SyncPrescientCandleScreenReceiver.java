package com.lying.tricksy.network;

import java.util.UUID;

import com.lying.tricksy.screen.PrescientCandleScreenHandler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;

@Environment(EnvType.CLIENT)
public class SyncPrescientCandleScreenReceiver implements ClientPlayNetworking.PlayChannelHandler
{
	public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
	{
		int syncId = buf.readInt();
		UUID id = buf.readUuid();
		
		client.execute(() -> 
		{
			if(client.player == null)
				return;
			
			ScreenHandler screenHandler = client.player.currentScreenHandler;
			if(syncId == screenHandler.syncId && screenHandler instanceof PrescientCandleScreenHandler)
			{
				PrescientCandleScreenHandler screen = (PrescientCandleScreenHandler)screenHandler;
				screen.setUUID(id);
			}
		});
	}
}