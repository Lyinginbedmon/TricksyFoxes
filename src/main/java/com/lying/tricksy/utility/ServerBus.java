package com.lying.tricksy.utility;

import com.lying.tricksy.component.TricksyComponent;
import com.lying.tricksy.init.TFComponents;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;

public class ServerBus
{
	public static void registerEventCallbacks()
	{
		ServerEntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD.register((originalEntity, newEntity, origin, destination) -> 
		{
			TricksyComponent compNew = TFComponents.TRICKSY_TRACKING.get(newEntity);
			compNew.cloneFrom(TFComponents.TRICKSY_TRACKING.get(originalEntity));
			
			RegistryKey<DimensionType> newDim = destination.getDimensionKey();
			Identifier dimID = newDim.getValue();
			if(!compNew.hasVisited(dimID))
				compNew.addVisited(dimID);
		});
	}
}
