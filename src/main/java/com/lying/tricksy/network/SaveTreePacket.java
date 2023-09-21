package com.lying.tricksy.network;

import java.util.List;
import java.util.UUID;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.BehaviourTree;
import com.lying.tricksy.reference.Reference;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
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
	
	public static class Receiver implements ServerPlayNetworking.PlayChannelHandler
	{
		public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
		{
			UUID tricksyID = buf.readUuid();
			NbtCompound treeNBT = buf.readNbt();
			
			List<PathAwareEntity> entities = player.getWorld().getEntitiesByClass(PathAwareEntity.class, player.getBoundingBox().expand(16D), (mob) -> mob instanceof ITricksyMob && mob.getUuid().equals(tricksyID));
			if(!entities.isEmpty())
				((ITricksyMob<?>)entities.get(0)).setBehaviourTree(treeNBT);
		}
	}
}
