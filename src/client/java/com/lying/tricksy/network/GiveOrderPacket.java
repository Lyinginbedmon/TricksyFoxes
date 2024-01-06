package com.lying.tricksy.network;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.entity.ai.whiteboard.OrderWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.OrderWhiteboard.Order;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

public class GiveOrderPacket
{
	public static final MinecraftClient mc = MinecraftClient.getInstance();
	
	public static void send(PlayerEntity player, @NotNull IWhiteboardObject<?> val, Order order, int color)
	{
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		
		OrderWhiteboard board = order.create(val);
		buffer.writeNbt(board.writeToNbt(new NbtCompound()));
		
		buffer.writeInt(color);
		
		ClientPlayNetworking.send(TFPacketHandler.GIVE_ORDER_ID, buffer);
	}
}
