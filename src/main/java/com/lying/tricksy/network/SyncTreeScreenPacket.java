package com.lying.tricksy.network;

import com.lying.tricksy.TricksyFoxes;
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;

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
		
		NbtList refList = new NbtList();
		addBoardToList(refList, tricksy.getLocalWhiteboard());
		addBoardToList(refList, tricksy.getGlobalWhiteboard());
		addBoardToList(refList, Whiteboard.CONSTANTS);
		NbtCompound data = new NbtCompound();
		data.put("References", refList);
		buffer.writeNbt(data);
		
		buffer.writeInt(TricksyFoxes.config.treeSizeCap());
		
		ServerPlayNetworking.send((ServerPlayerEntity)player, TFPacketHandler.SYNC_TREE_ID, buffer);
	}
	
	private static void addBoardToList(NbtList refList, Whiteboard<?> board)
	{
		board.allReferences().forEach((ref) -> 
		{
			NbtCompound data = new NbtCompound();
			data.put("Ref", ref.writeToNbt(new NbtCompound()));
			IWhiteboardObject<?> value = board.getValue(ref);
			if(!value.isEmpty())
				data.put("Val", board.getValue(ref).writeToNbt(new NbtCompound()));
			refList.add(data);
		});
	}
}