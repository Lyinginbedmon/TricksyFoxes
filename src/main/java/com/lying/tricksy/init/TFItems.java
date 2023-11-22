package com.lying.tricksy.init;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.lying.tricksy.item.ISealableItem;
import com.lying.tricksy.item.ItemPresciencePeriapt;
import com.lying.tricksy.item.ItemPrescientCandle;
import com.lying.tricksy.item.ItemPrescientNote;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class TFItems
{
    private static final Map<Identifier, Item> ITEMS = new HashMap<>();
    public static final List<Item> SEALABLES = Lists.newArrayList();
    
    public static final Item SAGE_HAT = register("sage_hat", new ItemSageHat(new FabricItemSettings()));
    public static final Item FOX_EGG = register("fox_spawn_egg", new SpawnEggItem(TFEntityTypes.TRICKSY_FOX, 13396256, 14005919, new FabricItemSettings()));
    
    public static final Item PRESCIENCE_ITEM = register("bottle_prescience", new BlockItem(TFBlocks.PRESCIENCE, new FabricItemSettings().rarity(Rarity.RARE)));
    public static final Item PRESCIENT_CANDLE_ITEM = register("prescient_candle", new ItemPrescientCandle(new FabricItemSettings().rarity(Rarity.RARE).maxCount(16)));
    public static final Item PERIAPT = register("periapt_prescience", new ItemPresciencePeriapt(new FabricItemSettings().rarity(Rarity.RARE)));
    public static final Item WORK_TABLE_ITEM = register("work_table", new BlockItem(TFBlocks.WORK_TABLE, new FabricItemSettings()));
    public static final Item CLOCKWORK_FRIAR_ITEM = register("clockwork_friar", new BlockItem(TFBlocks.CLOCKWORK_FRIAR, new FabricItemSettings()));
    
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
    
    public static final ItemGroup TRICKSY_GROUP = FabricItemGroup.builder().icon(() -> new ItemStack(SAGE_HAT)).displayName(Text.translatable("itemGroup."+Reference.ModInfo.MOD_ID+".item_group")).entries((ctx,entries) -> 
	    {
			entries.add(SAGE_HAT);
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
		});
    }
}
