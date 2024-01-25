package com.lying.tricksy.entity.ai.whiteboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.lying.tricksy.data.TFEntityTags;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjEntity;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.init.TFWhiteboards;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.utility.TricksyUtils;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ConstantsWhiteboard extends Whiteboard<Supplier<IWhiteboardObject<?>>>
{
	public static final WhiteboardRef ENT_MONSTERS = new WhiteboardRef("entity_monster", TFObjType.ENT).filter().displayName(Text.translatable("constant."+Reference.ModInfo.MOD_ID+".entity_monster"));
	public static final WhiteboardRef ENT_ANIMALS = new WhiteboardRef("entity_animal", TFObjType.ENT).filter().displayName(Text.translatable("constant."+Reference.ModInfo.MOD_ID+".entity_animal"));
	public static final WhiteboardRef ENT_PLAYERS = new WhiteboardRef("entity_player", TFObjType.ENT).filter().displayName(Text.translatable("constant."+Reference.ModInfo.MOD_ID+".entity_player"));
	
	public static WhiteboardObjEntity FILTER_MONSTER = new WhiteboardObjEntity().setFilter(true);
	public static WhiteboardObjEntity FILTER_ANIMAL = new WhiteboardObjEntity().setFilter(true);
	
	public static final Map<Direction, WhiteboardRef> DIRECTIONS = new HashMap<>();
	
	public ConstantsWhiteboard() { super(TFWhiteboards.CONSTANT, null); }
	
	public Whiteboard<?> build()
	{
		for(Direction dir : Direction.values())
		{
			WhiteboardRef ref = new WhiteboardRef("dir_"+dir.asString(), TFObjType.BLOCK, TFWhiteboards.CONSTANT).filter().displayName(TricksyUtils.translateDirection(dir));
			DIRECTIONS.put(dir, ref);
			register(ref, () -> new WhiteboardObjBlock(BlockPos.ORIGIN, dir));
		}
		
		register(ENT_MONSTERS, () -> FILTER_MONSTER.copy());
		register(ENT_ANIMALS, () -> FILTER_ANIMAL.copy());
		register(ENT_PLAYERS, () -> WhiteboardObjEntity.ofTypes(EntityType.PLAYER));
		return this;
	}
	
	public Whiteboard<Supplier<IWhiteboardObject<?>>> copy()
	{
		return CONSTANTS;
	}
	
	protected Supplier<IWhiteboardObject<?>> objectToSupplier(IWhiteboardObject<?> object) { return () -> object; }
	
	protected IWhiteboardObject<?> supplierToValue(Supplier<IWhiteboardObject<?>> supplier) { return supplier.get(); }
	
	/** Called by the server when the server starts or the data pack finishes reloading */
	public static void populateTagFilters()
	{
		FILTER_MONSTER = WhiteboardObjEntity.ofTypes(getOfTag(TFEntityTags.MONSTER)).setFilter(true);
		FILTER_ANIMAL = WhiteboardObjEntity.ofTypes(getOfTag(TFEntityTags.ANIMAL)).setFilter(true);
	}
	
	public static EntityType<?>[] getOfTag(TagKey<EntityType<?>> tagIn)
	{
		List<EntityType<?>> matches = Lists.newArrayList();
		for(Entry<RegistryKey<EntityType<?>>, EntityType<?>> type : Registries.ENTITY_TYPE.getEntrySet())
			if(type.getValue().isIn(tagIn))
				matches.add(type.getValue());
		return matches.toArray(new EntityType<?>[0]);
	}
}