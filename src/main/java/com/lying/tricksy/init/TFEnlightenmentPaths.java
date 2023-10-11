package com.lying.tricksy.init;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.lying.tricksy.component.Accomplishment;
import com.lying.tricksy.component.EnlightenmentPath;
import com.lying.tricksy.entity.EntityTricksyFox;
import com.lying.tricksy.entity.ITricksyMob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.FoxEntity;

public class TFEnlightenmentPaths
{
	/** Map of entity types to functions that convert mobs of that type to their enlightened equivalents */
	private static final Map<EntityType<? extends MobEntity>, EnlightenmentPath<?,?>> ENLIGHTEN_MAP = new HashMap<>();
	
	// TODO Allow server config of accomplishments per mob
	
	public static final EnlightenmentPath<FoxEntity, EntityTricksyFox> FOX	= addEnlightenment(EntityType.FOX, new EnlightenmentPath<FoxEntity, EntityTricksyFox>() 
		{
			public EntityTricksyFox enlighten(FoxEntity fox)
			{
				EntityTricksyFox tricksy = TFEntityTypes.TRICKSY_FOX.create(fox.getEntityWorld());
				tricksy.setVariant(fox.getVariant());
				tricksy.equipStack(EquipmentSlot.MAINHAND, fox.getEquippedStack(EquipmentSlot.MAINHAND));
				return tricksy;
			}
			
			public boolean conditionsMet(Collection<Accomplishment> accomplishments)
			{
				return 
						accomplishments.contains(TFAccomplishments.VISIT_NETHER) &&
						accomplishments.contains(TFAccomplishments.VISIT_OVERWORLD);
			}
		});
	
	private static <T extends PathAwareEntity, N extends PathAwareEntity & ITricksyMob<?>> EnlightenmentPath<T,N> addEnlightenment(EntityType<? extends MobEntity> type, EnlightenmentPath<T,N> path)
	{
		ENLIGHTEN_MAP.put(type, path);
		return path;
	}
	
	public static boolean isEnlightenable(MobEntity entity) { return ENLIGHTEN_MAP.containsKey(entity.getType()); }
	
	public static EnlightenmentPath<?, ?> getPath(EntityType<?> entity)
	{
		return ENLIGHTEN_MAP.get(entity);
	}
}
