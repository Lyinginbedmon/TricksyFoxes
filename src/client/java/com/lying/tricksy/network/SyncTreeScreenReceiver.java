package com.lying.tricksy.network;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBase;
import com.lying.tricksy.screen.TricksyTreeScreenHandler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Pair;

@Environment(EnvType.CLIENT)
public class SyncTreeScreenReceiver implements ClientPlayNetworking.PlayChannelHandler
{
	public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
	{
		int syncId = buf.readInt();
		UUID tricksyID = buf.readUuid();
		List<Pair<WhiteboardRef, IWhiteboardObject<?>>> references = unpackReferences(buf.readNbt().getList("References", NbtElement.COMPOUND_TYPE));
		int sizeCap = buf.readInt();
		
		client.execute(() -> 
		{
			if(client.player == null)
				return;
			
			PlayerEntity player = client.player;
			ScreenHandler screenHandler = client.player.currentScreenHandler;
			if(syncId == screenHandler.syncId && screenHandler instanceof TricksyTreeScreenHandler)
			{
				TricksyTreeScreenHandler screen = (TricksyTreeScreenHandler)screenHandler;
				screen.setUUID(tricksyID);
				screen.setCap(sizeCap);
				screen.setAvailableReferences(references);
				
				List<PathAwareEntity> entities = player.getWorld().getEntitiesByClass(PathAwareEntity.class, player.getBoundingBox().expand(16D), (mob) -> mob.getUuid().equals(tricksyID));
				if(!entities.isEmpty())
					screen.sync((ITricksyMob<?>)entities.get(0), entities.get(0));
			}
		});
	}
	
	public List<Pair<WhiteboardRef, IWhiteboardObject<?>>> unpackReferences(NbtList refList)
	{
		List<Pair<WhiteboardRef, IWhiteboardObject<?>>> references = Lists.newArrayList();
		for(int i=0; i<refList.size(); i++)
		{
			NbtCompound data = refList.getCompound(i);
			WhiteboardRef ref = WhiteboardRef.fromNbt(data.getCompound("Ref"));
			IWhiteboardObject<?> val = data.contains("Val", NbtElement.COMPOUND_TYPE) ? WhiteboardObjBase.createFromNbt(data.getCompound("Val")) : null;
			references.add(new Pair<>(ref, val));
		}
		return references;
	}
}