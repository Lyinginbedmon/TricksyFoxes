package com.lying.tricksy.entity.ai.whiteboard;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjEntity;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ConstantsWhiteboard extends Whiteboard<Supplier<IWhiteboardObject<?>>>
{
	public static final WhiteboardRef ENT_MONSTERS = new WhiteboardRef("entity_monster", TFObjType.ENT).filter().displayName(Text.translatable("constant."+Reference.ModInfo.MOD_ID+".entity_monster"));
	public static final WhiteboardRef ENT_ANIMALS = new WhiteboardRef("entity_animal", TFObjType.ENT).filter().displayName(Text.translatable("constant."+Reference.ModInfo.MOD_ID+".entity_animal"));
	public static final WhiteboardRef ENT_PLAYERS = new WhiteboardRef("entity_player", TFObjType.ENT).filter().displayName(Text.translatable("constant."+Reference.ModInfo.MOD_ID+".entity_player"));
	
	public static WhiteboardObjEntity FILTER_MONSTER = WhiteboardObjEntity.ofTypes(
			EntityType.BLAZE, 
			EntityType.CAVE_SPIDER, 
			EntityType.CREEPER, 
			EntityType.DROWNED, 
			EntityType.ELDER_GUARDIAN,
			EntityType.ENDER_DRAGON,
			EntityType.ENDERMAN,
			EntityType.ENDERMITE,
			EntityType.EVOKER,
			EntityType.GHAST,
			EntityType.GIANT,
			EntityType.GUARDIAN,
			EntityType.HOGLIN,
			EntityType.HUSK,
			EntityType.ILLUSIONER,
			EntityType.MAGMA_CUBE,
			EntityType.PHANTOM,
			EntityType.PIGLIN_BRUTE,
			EntityType.PILLAGER,
			EntityType.RAVAGER,
			EntityType.SHULKER,
			EntityType.SILVERFISH,
			EntityType.SKELETON,
			EntityType.SLIME,
			EntityType.SPIDER,
			EntityType.STRAY,
			EntityType.VEX,
			EntityType.VINDICATOR,
			EntityType.WARDEN,
			EntityType.WITCH,
			EntityType.WITHER,
			EntityType.WITHER_SKELETON,
			EntityType.ZOGLIN,
			EntityType.ZOMBIE,
			EntityType.ZOMBIE_VILLAGER);
	public static WhiteboardObjEntity FILTER_ANIMAL = WhiteboardObjEntity.ofTypes(
			EntityType.CHICKEN,
			EntityType.COW,
			EntityType.PIG,
			EntityType.MOOSHROOM,
			EntityType.SHEEP);
	
	public static final Map<Direction, WhiteboardRef> DIRECTIONS = new HashMap<>();
	
	public ConstantsWhiteboard() { super(BoardType.CONSTANT, null); }
	
	public Whiteboard<?> build()
	{
		for(Direction dir : Direction.values())
		{
			WhiteboardRef ref = new WhiteboardRef("dir_"+dir.asString(), TFObjType.BLOCK, BoardType.CONSTANT).filter().displayName(Text.literal(dir.name()));
			DIRECTIONS.put(dir, ref);
			register(ref, () -> new WhiteboardObjBlock(BlockPos.ORIGIN, dir));
		}
		
		register(ENT_MONSTERS, () -> FILTER_MONSTER.copy());
		register(ENT_ANIMALS, () -> FILTER_ANIMAL.copy());
		register(ENT_PLAYERS, () -> WhiteboardObjEntity.ofTypes(EntityType.PLAYER));
		return this;
	}
	
	protected Supplier<IWhiteboardObject<?>> objectToSupplier(IWhiteboardObject<?> object) { return () -> object; }
	
	protected IWhiteboardObject<?> supplierToValue(Supplier<IWhiteboardObject<?>> supplier) { return supplier.get(); }
}