package com.lying.tricksy.network;

import com.lying.tricksy.init.TFSoundEvents;
import com.lying.tricksy.reference.Reference;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class RefAddedReceiver implements ClientPlayNetworking.PlayChannelHandler
{
	public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
	{
		PlayerEntity player = client.player;
		if(player != null)
		{
			client.getSoundManager().play(PositionedSoundInstance.master(TFSoundEvents.WHITEBOARD_UPDATED, 1F));
			player.sendMessage(Text.translatable("item."+Reference.ModInfo.MOD_ID+".prescient_note.give_value.global"), true);
		}
	}
}