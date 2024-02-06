package com.lying.tricksy.init;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.lying.tricksy.item.ISealableItem;
import com.lying.tricksy.item.ItemOfuda;
import com.lying.tricksy.item.ItemPresciencePeriapt;
import com.lying.tricksy.item.ItemPrescientCandle;
import com.lying.tricksy.item.ItemPrescientNote;
import com.lying.tricksy.item.ItemSageFan;
import com.lying.tricksy.item.ItemSageHat;
import com.lying.tricksy.item.ItemScripture;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.utility.Region;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class TFItems
{
    private static final Map<Identifier, Item> ITEMS = new HashMap<>();
    public static final List<Item> SEALABLES = Lists.newArrayList();
    
    public static final Item SAGE_HAT = register("sage_hat", new ItemSageHat(new FabricItemSettings()));
    public static final Item SAGE_FAN = register("sage_fan", new ItemSageFan(new FabricItemSettings()));
    public static final Item WHITE_SAGE_FAN = register("white_sage_fan", new ItemSageFan(DyeColor.WHITE, new FabricItemSettings()));
    public static final Item ORANGE_SAGE_FAN = register("orange_sage_fan", new ItemSageFan(DyeColor.ORANGE, new FabricItemSettings()));
    public static final Item MAGENTA_SAGE_FAN = register("magenta_sage_fan", new ItemSageFan(DyeColor.MAGENTA, new FabricItemSettings()));
    public static final Item LIGHT_BLUE_SAGE_FAN = register("light_blue_sage_fan", new ItemSageFan(DyeColor.LIGHT_BLUE, new FabricItemSettings()));
    public static final Item YELLOW_SAGE_FAN = register("yellow_sage_fan", new ItemSageFan(DyeColor.YELLOW, new FabricItemSettings()));
    public static final Item LIME_SAGE_FAN = register("lime_sage_fan", new ItemSageFan(DyeColor.LIME, new FabricItemSettings()));
    public static final Item PINK_SAGE_FAN = register("pink_sage_fan", new ItemSageFan(DyeColor.PINK, new FabricItemSettings()));
    public static final Item GRAY_SAGE_FAN = register("gray_sage_fan", new ItemSageFan(DyeColor.GRAY, new FabricItemSettings()));
    public static final Item LIGHT_GRAY_SAGE_FAN = register("light_gray_sage_fan", new ItemSageFan(DyeColor.LIGHT_GRAY, new FabricItemSettings()));
    public static final Item CYAN_SAGE_FAN = register("cyan_sage_fan", new ItemSageFan(DyeColor.CYAN, new FabricItemSettings()));
    public static final Item PURPLE_SAGE_FAN = register("purple_sage_fan", new ItemSageFan(DyeColor.PURPLE, new FabricItemSettings()));
    public static final Item BLUE_SAGE_FAN = register("blue_sage_fan", new ItemSageFan(DyeColor.BLUE, new FabricItemSettings()));
    public static final Item BROWN_SAGE_FAN = register("brown_sage_fan", new ItemSageFan(DyeColor.BROWN, new FabricItemSettings()));
    public static final Item GREEN_SAGE_FAN = register("green_sage_fan", new ItemSageFan(DyeColor.GREEN, new FabricItemSettings()));
    public static final Item RED_SAGE_FAN = register("red_sage_fan", new ItemSageFan(DyeColor.RED, new FabricItemSettings()));
    public static final Item BLACK_SAGE_FAN = register("black_sage_fan", new ItemSageFan(DyeColor.BLACK, new FabricItemSettings()));
    
    public static final Item FOX_EGG = register("fox_spawn_egg", new SpawnEggItem(TFEntityTypes.TRICKSY_FOX, 13396256, 14005919, new FabricItemSettings()));
    public static final Item GOAT_EGG = register("goat_spawn_egg", new SpawnEggItem(TFEntityTypes.TRICKSY_GOAT, 5589310, 10851452, new FabricItemSettings()));
    public static final Item WOLF_EGG = register("wolf_spawn_egg", new SpawnEggItem(TFEntityTypes.TRICKSY_WOLF, 5589310, 10851452, new FabricItemSettings()));
    
    public static final Item PRESCIENCE_ITEM = register("bottle_prescience", new BlockItem(TFBlocks.PRESCIENCE, new FabricItemSettings().rarity(Rarity.RARE)));
    public static final Item PRESCIENT_CANDLE_ITEM = register("prescient_candle", new ItemPrescientCandle(new FabricItemSettings().rarity(Rarity.RARE).maxCount(16)));
    public static final Item PERIAPT = register("periapt_prescience", new ItemPresciencePeriapt(new FabricItemSettings().rarity(Rarity.RARE)));
    public static final Item WORK_TABLE_ITEM = register("work_table", new BlockItem(TFBlocks.WORK_TABLE, new FabricItemSettings()));
    public static final Item CLOCKWORK_FRIAR_ITEM = register("clockwork_friar", new BlockItem(TFBlocks.CLOCKWORK_FRIAR, new FabricItemSettings()));
    public static final Item OFUDA_ITEM = register("ofuda", new ItemOfuda(new FabricItemSettings()));
    
    public static final Item NOTE = register("prescient_note", new ItemPrescientNote(new FabricItemSettings().rarity(Rarity.UNCOMMON)));
    public static final Item NOTE_POS = register("prescient_note_block", new ItemPrescientNote.Block(new FabricItemSettings().rarity(Rarity.UNCOMMON)));
    public static final Item NOTE_ENT = register("prescient_note_entity", new ItemPrescientNote.Ent(new FabricItemSettings().rarity(Rarity.UNCOMMON)));
    public static final Item NOTE_REG = register("prescient_note_region", new ItemPrescientNote.Crafting<Region>(TFObjType.REGION, new FabricItemSettings().rarity(Rarity.UNCOMMON)));
    public static final Item NOTE_INT = register("prescient_note_number", new ItemPrescientNote.Crafting<Integer>(TFObjType.INT, new FabricItemSettings().rarity(Rarity.UNCOMMON)));
    public static final Item NOTE_BOOL = register("prescient_note_boolean", new ItemPrescientNote.Crafting<Boolean>(TFObjType.BOOL, new FabricItemSettings().rarity(Rarity.UNCOMMON)));
    public static final Item NOTE_ITEM = register("prescient_note_item", new ItemPrescientNote.Crafting<ItemStack>(TFObjType.ITEM, new FabricItemSettings().rarity(Rarity.UNCOMMON)));
    public static final Item SCRIPTURE = register("scripture", new ItemScripture(new FabricItemSettings().maxCount(1).rarity(Rarity.UNCOMMON)));
    
    public static final List<Item> NOTES = List.of(TFItems.NOTE_POS, TFItems.NOTE_REG, TFItems.NOTE_ENT, TFItems.NOTE_ITEM, TFItems.NOTE_INT, TFItems.NOTE_BOOL);
    public static final List<Item> NOTES_CYCLE = List.of(TFItems.NOTE_POS, TFItems.NOTE_ENT, TFItems.NOTE_ITEM, TFItems.NOTE_INT, TFItems.NOTE_BOOL);
    public static final Map<DyeColor, Item> FAN_COLOR_MAP = new HashMap<>();
    
    public static final ItemGroup TRICKSY_GROUP = FabricItemGroup.builder().icon(() -> new ItemStack(SAGE_HAT)).displayName(Text.translatable("itemGroup."+Reference.ModInfo.MOD_ID+".item_group")).entries((ctx,entries) -> 
	    {
			entries.add(SAGE_HAT);
			entries.add(SAGE_FAN);
			entries.add(PRESCIENCE_ITEM);
			entries.add(PRESCIENT_CANDLE_ITEM);
			entries.add(PERIAPT);
			entries.add(NOTE);
			entries.add(SCRIPTURE);
			entries.add(WORK_TABLE_ITEM);
			entries.add(CLOCKWORK_FRIAR_ITEM);
	    }).build();
    
    private static Item register(String nameIn, Item itemIn)
    {
    	ITEMS.put(new Identifier(Reference.ModInfo.MOD_ID, nameIn), itemIn);
    	if(itemIn instanceof ISealableItem)
    		SEALABLES.add(itemIn);
    	return itemIn;
    }
    
    public static void init()
    {
		for(Entry<Identifier, Item> entry : ITEMS.entrySet())
			Registry.register(Registries.ITEM, entry.getKey(), entry.getValue());
		
		Registry.register(Registries.ITEM_GROUP, new Identifier(Reference.ModInfo.MOD_ID, "item_group"), TRICKSY_GROUP);
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register((content) -> 
		{
			content.add(FOX_EGG);
			content.add(GOAT_EGG);
			content.add(WOLF_EGG);
		});
    }
    
    static
    {
		FAN_COLOR_MAP.put(DyeColor.WHITE, TFItems.WHITE_SAGE_FAN); 
		FAN_COLOR_MAP.put(DyeColor.ORANGE, TFItems.ORANGE_SAGE_FAN);
		FAN_COLOR_MAP.put(DyeColor.MAGENTA, TFItems.MAGENTA_SAGE_FAN);
		FAN_COLOR_MAP.put(DyeColor.LIGHT_BLUE, TFItems.LIGHT_BLUE_SAGE_FAN);
		FAN_COLOR_MAP.put(DyeColor.YELLOW, TFItems.YELLOW_SAGE_FAN);
		FAN_COLOR_MAP.put(DyeColor.LIME, TFItems.LIME_SAGE_FAN);
		FAN_COLOR_MAP.put(DyeColor.PINK, TFItems.PINK_SAGE_FAN);
		FAN_COLOR_MAP.put(DyeColor.GRAY, TFItems.GRAY_SAGE_FAN);
		FAN_COLOR_MAP.put(DyeColor.LIGHT_GRAY, TFItems.LIGHT_GRAY_SAGE_FAN);
		FAN_COLOR_MAP.put(DyeColor.CYAN, TFItems.CYAN_SAGE_FAN);
		FAN_COLOR_MAP.put(DyeColor.PURPLE, TFItems.PURPLE_SAGE_FAN);
		FAN_COLOR_MAP.put(DyeColor.BLUE, TFItems.BLUE_SAGE_FAN);
		FAN_COLOR_MAP.put(DyeColor.BROWN, TFItems.BROWN_SAGE_FAN);
		FAN_COLOR_MAP.put(DyeColor.GREEN, TFItems.GREEN_SAGE_FAN);
		FAN_COLOR_MAP.put(DyeColor.RED, TFItems.RED_SAGE_FAN);
		FAN_COLOR_MAP.put(DyeColor.BLACK, TFItems.BLACK_SAGE_FAN);
    }
}
