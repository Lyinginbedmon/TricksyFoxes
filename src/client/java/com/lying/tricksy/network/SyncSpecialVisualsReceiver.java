package com.lying.tricksy.network;

import com.lying.tricksy.utility.ClientBus;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

@Environment(EnvType.CLIENT)
public class SyncSpecialVisualsReceiver implements ClientPlayNetworking.PlayChannelHandler
{
	public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
	{
		NbtCompound saveData = buf.readNbt();
		
		client.execute(() -> 
		{
			if(client.player == null)
				return;
			ClientBus.getSpecialVisuals().readFromNbt(saveData);
		});
	}
}