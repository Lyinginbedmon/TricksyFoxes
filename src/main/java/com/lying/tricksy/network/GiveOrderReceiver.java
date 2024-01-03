package com.lying.tricksy.network;

import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.whiteboard.OrderWhiteboard;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayChannelHandler;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class GiveOrderReceiver implements PlayChannelHandler
{
	public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
	{
		OrderWhiteboard board = new OrderWhiteboard();
		board.setWorld(player.getWorld());
		board.readFromNbt(buf.readNbt());
		
		World world = player.getWorld();
		world.getEntitiesByClass(PathAwareEntity.class, player.getBoundingBox().expand(16D), (mob) -> mob instanceof ITricksyMob<?> && mob.isAlive()).forEach(mob -> 
		{
			ITricksyMob<?> tricksy = (ITricksyMob<?>)mob;
			if(tricksy.isSage(player) && mob.canSee(player))
				tricksy.giveCommand(board);
		});
	}
}
