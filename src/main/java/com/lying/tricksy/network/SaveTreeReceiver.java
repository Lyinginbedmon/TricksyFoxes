package com.lying.tricksy.network;

import java.util.List;
import java.util.UUID;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.reference.Reference;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayChannelHandler;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class SaveTreeReceiver implements PlayChannelHandler
{
	public static final Identifier PACKET_ID = new Identifier(Reference.ModInfo.MOD_ID, "save_tree");
	
	public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
	{
		UUID tricksyID = buf.readUuid();
		NbtCompound treeNBT = buf.readNbt();
		
		List<PathAwareEntity> entities = player.getWorld().getEntitiesByClass(PathAwareEntity.class, player.getBoundingBox().expand(16D), (mob) -> mob instanceof ITricksyMob && mob.getUuid().equals(tricksyID));
		if(!entities.isEmpty())
			((ITricksyMob<?>)entities.get(0)).setBehaviourTree(treeNBT);
	}
}
