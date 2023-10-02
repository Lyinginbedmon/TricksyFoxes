package com.lying.tricksy.utility;

import com.lying.tricksy.component.Accomplishment;
import com.lying.tricksy.component.TricksyComponent;
import com.lying.tricksy.init.TFAccomplishments;
import com.lying.tricksy.init.TFComponents;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;

public class ServerBus
{
	public static void registerEventCallbacks()
	{
		ServerEntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD.register((originalEntity, newEntity, origin, destination) -> 
		{
			TricksyComponent compNew = TFComponents.TRICKSY_TRACKING.get(newEntity);
			compNew.cloneFrom(TFComponents.TRICKSY_TRACKING.get(originalEntity));
			
			if(!compNew.hasAchieved(TFAccomplishments.DIMENSIONAL_TRAVEL))
				compNew.addAccomplishment(TFAccomplishments.DIMENSIONAL_TRAVEL);
			
			RegistryKey<DimensionType> newDim = destination.getDimensionKey();
			Accomplishment accomplishment = null;
			if(newDim == DimensionTypes.THE_NETHER)
				accomplishment = TFAccomplishments.VISIT_NETHER;
			else if(newDim == DimensionTypes.THE_END)
				accomplishment = TFAccomplishments.VISIT_END;
			else if(newDim == DimensionTypes.OVERWORLD)
				accomplishment = TFAccomplishments.VISIT_OVERWORLD;
			
			if(accomplishment != null && !compNew.hasAchieved(accomplishment))
				compNew.addAccomplishment(accomplishment);
		});
	}
}
