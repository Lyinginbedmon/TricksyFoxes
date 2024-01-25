package com.lying.tricksy.network;

import java.util.UUID;

import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFWhiteboards;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

public class AddLocalReferencePacket
{
	public static <T extends PathAwareEntity & ITricksyMob<?>> void send(PlayerEntity player, UUID tricksyID, WhiteboardRef reference)
	{
		if(reference.boardType() != TFWhiteboards.LOCAL || reference.uncached())
			return;
		
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeUuid(tricksyID);
		buffer.writeNbt(reference.writeToNbt(new NbtCompound()));
		ClientPlayNetworking.send(TFPacketHandler.ADD_LOCAL_REF_ID, buffer);
	}
}
