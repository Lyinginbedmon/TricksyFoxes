package com.lying.tricksy.network;

import com.lying.tricksy.init.TFItems;
import com.lying.tricksy.item.ItemScripture;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayChannelHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

public class ToggleScriptureOverruleReceiver implements PlayChannelHandler
{
	public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
	{
		boolean setting = buf.readBoolean();
		for(Hand hand : Hand.values())
		{
			ItemStack stack = player.getStackInHand(hand);
			if(stack.getItem() == TFItems.SCRIPTURE)
			{
				ItemScripture.setOverrule(stack, setting);
				player.setStackInHand(hand, stack);
				break;
			}
		}
	}
}
