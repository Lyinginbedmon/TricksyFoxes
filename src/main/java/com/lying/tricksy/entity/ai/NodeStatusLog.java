package com.lying.tricksy.entity.ai;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.whiteboard.CommandWhiteboard.Order;
import com.lying.tricksy.reference.Reference;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Pair;

public class NodeStatusLog
{
	private Map<UUID, Log> log = new HashMap<>();
	
	private Order tree = null;
	
	public void setTree(Order orderIn) { this.tree = orderIn; }

	public Order tree() { return this.tree; }
	
	public void clear() { this.log.clear(); }
	
	public void tick()
	{
		List<UUID> dead = Lists.newArrayList();
		for(Entry<UUID, Log> entry : log.entrySet())
		{
			int ticks = entry.getValue().getRight() - 1;
			if(ticks <= 0)
				dead.add(entry.getKey());
			else
				entry.getValue().setRight(ticks);
		}
		
		dead.forEach(id -> log.remove(id));
	}
	
	public void logCold(UUID idIn)
	{
		putStatus(idIn, new Log());
	}
	
	public void logStatus(UUID idIn, Result resultIn)
	{
		putStatus(idIn, new Log(resultIn, false));
	}
	
	protected void putStatus(UUID idIn, Log logIn)
	{
		log.put(idIn, logIn);
	}
	
	/** Returns the log of the given node UUID, if it exists */
	@Nullable
	public Log getLog(UUID uuid)
	{
		return log.getOrDefault(uuid, null);
	}
	
	/** Returns a collection of node UUIDs that were recently active */
	public Collection<UUID> getActiveNodes() { return log.keySet(); }
	
	/** Returns true if the given node was active in the most recent tick */
	public boolean wasActive(TreeNode<?> node)
	{
		return wasActive(node.getID());
	}
	
	/** Returns true if the given node UUID was active in the most recent tick */
	public boolean wasActive(UUID idIn)
	{
		return getLog(idIn) != null && getLog(idIn).getRight() == Log.DURATION;
	}
	
	public NbtCompound writeToNbt(NbtCompound nbt)
	{
		if(this.tree != null)
			nbt.putString("Tree", this.tree.asString());
		
		NbtList list = new NbtList();
		log.entrySet().forEach(entry -> 
		{
			NbtCompound data = new NbtCompound();
			data.putUuid("ID", entry.getKey());
			data.put("Result", entry.getValue().toNbt(new NbtCompound()));
			list.add(data);
		});
		nbt.put("Data", list);
		return nbt;
	}
	
	public static NodeStatusLog fromNbt(NbtCompound nbt)
	{
		NodeStatusLog log = new NodeStatusLog();
		
		if(nbt.contains("Tree", NbtElement.STRING_TYPE))
			log.setTree(Order.fromString(nbt.getString("Tree")));
		
		if(nbt.contains("Data", NbtElement.LIST_TYPE))
		{
			NbtList list = nbt.getList("Data", NbtElement.COMPOUND_TYPE);
			for(int i=0; i<list.size(); i++)
			{
				NbtCompound data = list.getCompound(i);
				log.putStatus(data.getUuid("ID"), Log.fromNbt(data.getCompound("Result")));
			}
		}
		
		return log;
	}
	
	public static class Log extends Pair<Result, Integer>
	{
		public static final int DURATION = Reference.Values.TICKS_PER_SECOND;
		private final boolean isCold;
		
		public Log()
		{
			this(Result.FAILURE, true);
		}
		
		public Log(Result resultIn, boolean isCold)
		{
			super(resultIn, DURATION);
			this.isCold = isCold;
		}
		
		public NbtCompound toNbt(NbtCompound data)
		{
			data.putString("Value", getLeft().asString());
			data.putBoolean("Cold", isCold);
			data.putInt("Ticks", getRight());
			return data;
		}
		
		public static Log fromNbt(NbtCompound data)
		{
			Log log = new Log(Result.fromString(data.getString("Value")), data.getBoolean("Cold"));
			log.setRight(data.getInt("Ticks"));
			return log;
		}
		
		public boolean onCooldown() { return this.isCold; }
	}
}
