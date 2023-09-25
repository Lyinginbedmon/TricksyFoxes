package com.lying.tricksy.network;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.screen.TreeScreenHandler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;

@Environment(EnvType.CLIENT)
public class SyncTreeScreenReceiver implements ClientPlayNetworking.PlayChannelHandler
{
	public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
	{
		int syncId = buf.readInt();
		UUID tricksyID = buf.readUuid();
		
		NbtList refList = buf.readNbt().getList("References", NbtElement.COMPOUND_TYPE);
		List<WhiteboardRef> references = Lists.newArrayList();
		for(int i=0; i<refList.size(); i++)
			references.add(WhiteboardRef.fromNbt(refList.getCompound(i)));
		
		client.execute(() -> 
		{
			if(client.player == null)
				return;
			
			PlayerEntity player = client.player;
			ScreenHandler screenHandler = client.player.currentScreenHandler;
			if(syncId == screenHandler.syncId && screenHandler instanceof TreeScreenHandler)
			{
				List<PathAwareEntity> entities = player.getWorld().getEntitiesByClass(PathAwareEntity.class, player.getBoundingBox().expand(16D), (mob) -> mob.getUuid().equals(tricksyID));
				if(!entities.isEmpty())
				{
					TreeScreenHandler screen = (TreeScreenHandler)screenHandler;
					screen.sync((ITricksyMob<?>)entities.get(0), entities.get(0).getUuid());
					screen.setAvailableReferences(references);
				}
			}
		});
	}
}