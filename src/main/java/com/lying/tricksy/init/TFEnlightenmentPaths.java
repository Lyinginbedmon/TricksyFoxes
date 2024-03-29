package com.lying.tricksy.init;

import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.component.ConfigurablePath;
import com.lying.tricksy.component.EnlightenmentPath;
import com.lying.tricksy.entity.EntityTricksyFox;
import com.lying.tricksy.entity.EntityTricksyGoat;
import com.lying.tricksy.entity.EntityTricksyWolf;
import com.lying.tricksy.reference.Reference;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

public class TFEnlightenmentPaths implements SimpleResourceReloadListener<List<JsonObject>>
{
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
	public static final String FILE_PATH = "enlightenment_paths";
	
	/** Map of entity types to functions that convert mobs of that type to their enlightened equivalents */
	private static final Map<EntityType<? extends MobEntity>, EnlightenmentPath<?,?>> DEFAULT_MAP = new HashMap<>();
	
	private final Map<EntityType<? extends MobEntity>, EnlightenmentPath<?,?>> PATHS_MAP = new HashMap<>();
	
	public static TFEnlightenmentPaths INSTANCE;
	
	public static final EnlightenmentPath<FoxEntity, EntityTricksyFox> FOX	= addEnlightenment(EntityType.FOX, new ConfigurablePath<FoxEntity, EntityTricksyFox>(EntityType.FOX, TFAccomplishments.VISIT_NETHER, TFAccomplishments.VISIT_OVERWORLD) 
		{
			public EntityType<EntityTricksyFox> resultType(){ return TFEntityTypes.TRICKSY_FOX; }
			
			public EntityTricksyFox enlighten(FoxEntity fox)
			{
				EntityTricksyFox tricksy = TFEntityTypes.TRICKSY_FOX.create(fox.getEntityWorld());
				tricksy.setVariant(fox.getVariant());
				tricksy.equipStack(EquipmentSlot.MAINHAND, fox.getEquippedStack(EquipmentSlot.MAINHAND));
				tricksy.equipStack(EquipmentSlot.OFFHAND, fox.getEquippedStack(EquipmentSlot.OFFHAND));
				return tricksy;
			}
		}.setMastery(TFAccomplishments.VISIT_END, TFAccomplishments.JOURNEYMAN));
	
	public static final EnlightenmentPath<GoatEntity, EntityTricksyGoat> GOAT	= addEnlightenment(EntityType.GOAT, new ConfigurablePath<GoatEntity, EntityTricksyGoat>(EntityType.GOAT, TFAccomplishments.CLOUDSEEKER, TFAccomplishments.JOURNEYMAN)
		{
			public EntityType<EntityTricksyGoat> resultType(){ return TFEntityTypes.TRICKSY_GOAT; }
			
			public EntityTricksyGoat enlighten(GoatEntity goat)
			{
				EntityTricksyGoat tricksy = TFEntityTypes.TRICKSY_GOAT.create(goat.getEntityWorld());
				tricksy.setHorns(goat.hasLeftHorn(), goat.hasRightHorn());
				tricksy.setScreaming(goat.isScreaming());
				tricksy.equipStack(EquipmentSlot.MAINHAND, goat.getEquippedStack(EquipmentSlot.MAINHAND));
				tricksy.equipStack(EquipmentSlot.OFFHAND, goat.getEquippedStack(EquipmentSlot.OFFHAND));
				return tricksy;
			}
		}.setMastery(TFAccomplishments.OUTSIDE_THE_BOX, TFAccomplishments.EYES_UNCLOUDED));
	
	public static final EnlightenmentPath<WolfEntity, EntityTricksyWolf> WOLF 	= addEnlightenment(EntityType.WOLF, new ConfigurablePath<WolfEntity, EntityTricksyWolf>(EntityType.WOLF, TFAccomplishments.SCHOLAR, TFAccomplishments.ARCHAEOLOGIST)
		{
			public EntityType<EntityTricksyWolf> resultType() { return TFEntityTypes.TRICKSY_WOLF; }
			
			public EntityTricksyWolf enlighten(WolfEntity wolf)
			{
				EntityTricksyWolf tricksy = TFEntityTypes.TRICKSY_WOLF.create(wolf.getEntityWorld());
				tricksy.equipStack(EquipmentSlot.MAINHAND, wolf.getEquippedStack(EquipmentSlot.MAINHAND));
				tricksy.equipStack(EquipmentSlot.OFFHAND, wolf.getEquippedStack(EquipmentSlot.OFFHAND));
				return tricksy;
			}
		}.setMastery(TFAccomplishments.VISIT_END, TFAccomplishments.SQUIRE));
	
	private static <T extends PathAwareEntity, N extends PathAwareEntity & ITricksyMob<?>> EnlightenmentPath<T,N> addEnlightenment(EntityType<? extends MobEntity> type, EnlightenmentPath<T,N> path)
	{
		DEFAULT_MAP.put(type, path);
		return path;
	}
	
	public static Collection<EnlightenmentPath<?,?>> getDefaultPaths() { return DEFAULT_MAP.values(); }
	
	public static void init()
	{
		INSTANCE = new TFEnlightenmentPaths();
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(TFEnlightenmentPaths.INSTANCE);
	}
	
	public TFEnlightenmentPaths()
	{
		DEFAULT_MAP.entrySet().forEach(entry -> PATHS_MAP.put(entry.getKey(), entry.getValue()));
	}
	
	/** Returns true if any enlightenment path exists for the given mob's entity type */
	public boolean isEnlightenable(MobEntity entity) { return !ITricksyMob.class.isAssignableFrom(entity.getClass()) && PATHS_MAP.containsKey(entity.getType()); }
	
	/** Returns true if any enlightenment path ends in a mob with the same entity type as the given mob */
	public boolean isEnlightened(MobEntity entity)
	{
		return ITricksyMob.class.isAssignableFrom(entity.getClass()) && PATHS_MAP.values().stream().anyMatch((path) -> entity.getType() == path.resultType());
	}
	
	@Nullable
	public EnlightenmentPath<?, ?> getPathFrom(EntityType<?> entity) { return PATHS_MAP.getOrDefault(entity, null); }
	
	@Nullable
	public EnlightenmentPath<?, ?> getPathTo(EntityType<?> entity)
	{
		for(EnlightenmentPath<?, ?> entry : PATHS_MAP.values())
			if(entry.resultType() == entity)
				return entry;
		
		return null;
	}
	
	@Nullable
	public EnlightenmentPath<?, ?> getPath(Identifier regName)
	{
		for(EnlightenmentPath<?, ?> path : PATHS_MAP.values())
			if(path.registryName().equals(regName))
				return path;
		return null;
	}
	
	public Identifier getFabricId()
	{
		return new Identifier(Reference.ModInfo.MOD_ID, "enlightenment_paths");
	}
	
	public CompletableFuture<List<JsonObject>> load(ResourceManager manager, Profiler profiler, Executor executor)
	{
		return CompletableFuture.supplyAsync(() -> 
		{
			List<JsonObject> objects = Lists.newArrayList();
			manager.findAllResources(FILE_PATH, Predicates.alwaysTrue()).values().forEach((fileSet) -> 
			{
				for(Resource file : fileSet)
				{
					try
					{
						JsonObject element = JsonHelper.deserialize(GSON, (Reader)file.getReader(), JsonObject.class);
						if(element.has("EntityType"))
							objects.add(element);
					}
					catch(Exception e) { }
				}
			});
			return objects;
		});
	}
	
	public CompletableFuture<Void> apply(List<JsonObject> data, ResourceManager manager, Profiler profiler, Executor executor)
	{
		return CompletableFuture.runAsync(() -> 
		{
			for(JsonObject prep : data)
			{
				if(!prep.has("EntityType"))
					continue;
				
				Identifier regName = new Identifier(prep.get("EntityType").getAsString());
				EnlightenmentPath<?,?> path = getPath(regName);
				if(path == null)
					continue;
				
				path.readFromJson(prep);
			}
		});
	}
}
