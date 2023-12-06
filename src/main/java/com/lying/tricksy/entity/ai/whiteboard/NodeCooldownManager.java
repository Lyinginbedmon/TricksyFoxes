package com.lying.tricksy.entity.ai.whiteboard;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.lying.tricksy.entity.ai.node.subtype.NodeSubType;

public class NodeCooldownManager
{
	private Map<NodeSubType<?>, Integer> values = new HashMap<>();
	private int tick = 0;
	
	public void update()
	{
		tick++;
		if(!values.isEmpty())
		{
			Iterator<Map.Entry<NodeSubType<?>, Integer>> iterator = values.entrySet().iterator();
			while(iterator.hasNext())
			{
				Map.Entry<NodeSubType<?>, Integer> entry = iterator.next();
				if(entry.getValue() <= tick)
					iterator.remove();
			}
		}
	}
	
	public boolean isOnCooldown(NodeSubType<?> type)
	{
		return values.keySet().stream().anyMatch(node -> node.getRegistryName().equals(type.getRegistryName()));
	}
	
	public void putOnCooldown(NodeSubType<?> type, int duration)
	{
		values.put(type, this.tick + duration);
	}
}