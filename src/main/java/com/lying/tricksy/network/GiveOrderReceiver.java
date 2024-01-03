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
		
		int color = buf.readInt();
		
		World world = player.getWorld();
		world.getEntitiesByClass(PathAwareEntity.class, player.getBoundingBox().expand(16D), (mob) -> mob instanceof ITricksyMob<?> && mob.isAlive()).forEach(mob -> 
		{
			ITricksyMob<?> tricksy = (ITricksyMob<?>)mob;
			if(mob.canSee(player) && willReceiveOrder(tricksy, player, color))
				tricksy.giveCommand(board);
		});
	}
	
	/** Returns true if the tricksy recognises the player as its sage and its color matches the fan color or is non-specific */
	private static boolean willReceiveOrder(ITricksyMob<?> tricksy, ServerPlayerEntity player, int fanColor)
	{
		return tricksy.isSage(player) && (!tricksy.hasColor() || fanColor == tricksy.getColor());
	}
}
