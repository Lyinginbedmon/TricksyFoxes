package com.lying.tricksy.network;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ai.BehaviourTree;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

public class SaveTreePacket
{
	public static void send(PlayerEntity player, @Nullable UUID tricksyID, BehaviourTree tree)
	{
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeUuid(tricksyID == null ? UUID.randomUUID() : tricksyID);
		buffer.writeNbt(tree.storeInNbt());
		ClientPlayNetworking.send(TFPacketHandler.SAVE_TREE_ID, buffer);
	}
}
