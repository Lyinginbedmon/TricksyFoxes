package com.lying.tricksy.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.reference.Reference;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

public class ServerWhiteboards extends PersistentState
{
	private Map<UUID, GlobalWhiteboard> whiteboards = new HashMap<>();
	
	@Nullable
	private ServerWorld world;
	
	public static ServerWhiteboards getServerWhiteboards(MinecraftServer server)
	{
		ServerWorld world = server.getWorld(World.OVERWORLD);
		PersistentStateManager manager = world.getPersistentStateManager();
		ServerWhiteboards whiteboards = manager.getOrCreate(ServerWhiteboards::createFromNbt, ServerWhiteboards::new, Reference.ModInfo.MOD_ID);
		whiteboards.world = world;
		whiteboards.markDirty();
		return whiteboards;
	}
	
	public NbtCompound writeNbt(NbtCompound nbt)
	{
		NbtList set = new NbtList();
		whiteboards.forEach((sage,board) -> 
		{
			NbtCompound compound = new NbtCompound();
			compound.putUuid("ID", sage);
			compound.put("Board", board.writeToNbt(new NbtCompound()));
			set.add(compound);
		});
		nbt.put("Data", set);
		return nbt;
	}
	
	public static ServerWhiteboards createFromNbt(NbtCompound nbt)
	{
		ServerWhiteboards boards = new ServerWhiteboards();
		NbtList set = nbt.getList("Data", NbtElement.COMPOUND_TYPE);
		for(int i=0; i<set.size(); i++)
		{
			NbtCompound compound = set.getCompound(i);
			UUID sage = compound.getUuid("ID");
			GlobalWhiteboard board = new GlobalWhiteboard(null);
			board.readFromNbt(compound.getCompound("Board"));
			boards.whiteboards.put(sage, board);
		}
		return boards;
	}
	
	public GlobalWhiteboard getWhiteboardFor(UUID sageID)
	{
		if(!whiteboards.containsKey(sageID))
		{
			whiteboards.put(sageID, new GlobalWhiteboard(this.world));
			markDirty();
		}
		GlobalWhiteboard board = whiteboards.get(sageID);
		board.setWorld(world);
		return board;
	}
}
