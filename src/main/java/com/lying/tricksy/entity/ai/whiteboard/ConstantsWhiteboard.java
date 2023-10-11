package com.lying.tricksy.entity.ai.whiteboard;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ConstantsWhiteboard extends Whiteboard<Supplier<IWhiteboardObject<?>>>
{
	public static final WhiteboardRef NUM_1 = new WhiteboardRef("number_1", TFObjType.INT, BoardType.CONSTANT).displayName(Text.literal("1"));
	public static final WhiteboardRef NUM_2 = new WhiteboardRef("number_2", TFObjType.INT, BoardType.CONSTANT).displayName(Text.literal("2"));
	public static final WhiteboardRef NUM_3 = new WhiteboardRef("number_3", TFObjType.INT, BoardType.CONSTANT).displayName(Text.literal("3"));
	public static final WhiteboardRef NUM_4 = new WhiteboardRef("number_4", TFObjType.INT, BoardType.CONSTANT).displayName(Text.literal("4"));
	public static final WhiteboardRef NUM_5 = new WhiteboardRef("number_5", TFObjType.INT, BoardType.CONSTANT).displayName(Text.literal("5"));
	public static final WhiteboardRef NUM_8 = new WhiteboardRef("number_8", TFObjType.INT, BoardType.CONSTANT).displayName(Text.literal("8"));
	public static final WhiteboardRef NUM_16 = new WhiteboardRef("number_16", TFObjType.INT, BoardType.CONSTANT).displayName(Text.literal("16"));
	public static final WhiteboardRef NUM_32 = new WhiteboardRef("number_32", TFObjType.INT, BoardType.CONSTANT).displayName(Text.literal("32"));
	public static final WhiteboardRef NUM_64 = new WhiteboardRef("number_64", TFObjType.INT, BoardType.CONSTANT).displayName(Text.literal("64"));
	public static final WhiteboardRef BOOL_TRUE = new WhiteboardRef("boolean_true", TFObjType.BOOL, BoardType.CONSTANT).displayName(new WhiteboardObj.Bool(true).describe().get(0));
	public static final WhiteboardRef BOOL_FALSE = new WhiteboardRef("boolean_false", TFObjType.BOOL, BoardType.CONSTANT).displayName(new WhiteboardObj.Bool(false).describe().get(0));
	public static final WhiteboardRef ENT_MONSTERS = new WhiteboardRef("entity_monster", TFObjType.ENT, BoardType.CONSTANT).displayName(Text.translatable("constant."+Reference.ModInfo.MOD_ID+".entity_monster"));
	public static final WhiteboardRef ENT_ANIMALS = new WhiteboardRef("entity_animal", TFObjType.ENT, BoardType.CONSTANT).displayName(Text.translatable("constant."+Reference.ModInfo.MOD_ID+".entity_animal"));
	public static final WhiteboardRef ENT_PLAYERS = new WhiteboardRef("entity_player", TFObjType.ENT, BoardType.CONSTANT).displayName(Text.translatable("constant."+Reference.ModInfo.MOD_ID+".entity_player"));
	
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
		register(NUM_1, () -> new WhiteboardObj.Int(1));
		register(NUM_2, () -> new WhiteboardObj.Int(2));
		register(NUM_3, () -> new WhiteboardObj.Int(3));
		register(NUM_4, () -> new WhiteboardObj.Int(4));
		register(NUM_5, () -> new WhiteboardObj.Int(5));
		register(NUM_8, () -> new WhiteboardObj.Int(8));
		register(NUM_16, () -> new WhiteboardObj.Int(16));
		register(NUM_32, () -> new WhiteboardObj.Int(32));
		register(NUM_64, () -> new WhiteboardObj.Int(64));
		register(BOOL_TRUE, () -> new WhiteboardObj.Bool(true));
		register(BOOL_FALSE, () -> new WhiteboardObj.Bool(false));
		for(Direction dir : Direction.values())
		{
			WhiteboardRef ref = new WhiteboardRef("dir_"+dir.asString(), TFObjType.BLOCK, BoardType.CONSTANT).displayName(Text.literal(dir.name()));
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