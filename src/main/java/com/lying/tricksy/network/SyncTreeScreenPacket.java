package com.lying.tricksy.network;

import com.lying.tricksy.TricksyFoxes;
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.component.TricksyComponent;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class SyncTreeScreenPacket
{
	public static <T extends PathAwareEntity & ITricksyMob<?>> void send(PlayerEntity player, T tricksy, int syncId)
	{
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeInt(syncId);
		buffer.writeUuid(tricksy.getUuid());
		boolean isMaster = TricksyComponent.isMobMaster(tricksy);
		buffer.writeBoolean(isMaster);
		
		NbtList refList = tricksy.getWhiteboards().addToList(new NbtList());
		Whiteboard.CONSTANTS.addReferencesToList(refList);
		if(isMaster)
			tricksy.getBehaviourTree().order().addReferencesToList(refList);
		
		NbtCompound data = new NbtCompound();
		data.put("References", refList);
		buffer.writeNbt(data);
		
		buffer.writeInt(TricksyFoxes.config.treeSizeCap());
		
		ServerPlayNetworking.send((ServerPlayerEntity)player, TFPacketHandler.SYNC_TREE_ID, buffer);
	}
}