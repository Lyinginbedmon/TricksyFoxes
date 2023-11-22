package com.lying.tricksy.entity.ai;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class NodeStatusLog
{
	private Map<UUID, Result> log = new HashMap<>();
	
	public void clear() { this.log.clear(); }
	
	public void logStatus(UUID idIn, Result resultIn)
	{
		log.put(idIn, resultIn);
	}
	
	@Nullable
	public Result getLog(UUID uuid)
	{
		return log.getOrDefault(uuid, null);
	}
	
	public Collection<UUID> getActiveNodes() { return log.keySet(); }
	
	public boolean wasActive(TreeNode<?> node)
	{
		return wasActive(node.getID()) || (!node.isRoot() && wasActive(node.parent().getID()));
	}
	
	public boolean wasActive(UUID idIn)
	{
		return getLog(idIn) != null;
	}
	
	public NbtCompound writeToNbt(NbtCompound nbt)
	{
		NbtList list = new NbtList();
		log.entrySet().forEach(entry -> 
		{
			NbtCompound data = new NbtCompound();
			data.putUuid("ID", entry.getKey());
			data.putString("Result", entry.getValue().toString());
			list.add(data);
		});
		nbt.put("Data", list);
		return nbt;
	}
	
	public static NodeStatusLog fromNbt(NbtCompound nbt)
	{
		NodeStatusLog log = new NodeStatusLog();
		
		if(nbt.contains("Data", NbtElement.LIST_TYPE))
		{
			NbtList list = nbt.getList("Data", NbtElement.COMPOUND_TYPE);
			for(int i=0; i<list.size(); i++)
			{
				NbtCompound data = list.getCompound(i);
				Result result = Result.fromString(data.getString("Result"));
				if(result != null)
					log.logStatus(data.getUuid("ID"), result);
			}
		}
		
		return log;
	}
}
