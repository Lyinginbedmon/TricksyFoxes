package com.lying.tricksy.network;

import java.util.UUID;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.BehaviourTree;
import com.lying.tricksy.reference.Reference;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class SaveTreePacket
{
	public static final Identifier PACKET_ID = new Identifier(Reference.ModInfo.MOD_ID, "save_tree");
	
	public static <T extends PathAwareEntity & ITricksyMob<?>> void send(PlayerEntity player, UUID tricksyID, BehaviourTree tree)
	{
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeUuid(tricksyID);
		buffer.writeNbt(tree.storeInNbt());
		ClientPlayNetworking.send(PACKET_ID, buffer);
	}
}
