package com.lying.tricksy.network;

import java.util.List;
import java.util.UUID;

import com.lying.tricksy.TricksyFoxes;
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFWhiteboards;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayChannelHandler;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class DeleteReferenceReceiver implements PlayChannelHandler
{
	public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
	{
		UUID tricksyID = buf.readUuid();
		WhiteboardRef reference = WhiteboardRef.fromNbt(buf.readNbt());
		if(reference.boardType() == TFWhiteboards.CONSTANT)
		{
			TricksyFoxes.LOGGER.error("Received packet to delete value from constants whiteboard");
			return;
		}
		
		List<PathAwareEntity> entities = player.getWorld().getEntitiesByClass(PathAwareEntity.class, player.getBoundingBox().expand(16D), (mob) -> mob instanceof ITricksyMob && mob.getUuid().equals(tricksyID));
		if(!entities.isEmpty())
		{
			ITricksyMob<?> tricksy = (ITricksyMob<?>)entities.get(0);
			if(reference.boardType() == TFWhiteboards.GLOBAL)
				tricksy.getGlobalWhiteboard().delete(reference);
			else if(reference.boardType() == TFWhiteboards.LOCAL)
				tricksy.getLocalWhiteboard().delete(reference);
		}
	}
}
