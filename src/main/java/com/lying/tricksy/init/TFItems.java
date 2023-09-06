package com.lying.tricksy.init;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.lying.tricksy.item.ItemSageHat;
import com.lying.tricksy.reference.Reference;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TFItems
{
    private static final Map<Identifier, Item> ITEMS = new HashMap<>();
    
    public static final Item SAGE_HAT = register("sage_hat", new ItemSageHat(new FabricItemSettings()));
    public static final Item FOX_EGG = register("fox_spawn_egg", new SpawnEggItem(TFEntityTypes.TRICKSY_FOX, 13396256, 14005919, new FabricItemSettings()));
    
    public static final ItemGroup TRICKSY_GROUP = FabricItemGroup.builder().icon(() -> new ItemStack(SAGE_HAT)).displayName(Text.translatable("itemGroup."+Reference.ModInfo.MOD_ID+".item_group")).entries((ctx,entries) -> 
	    {
			entries.add(SAGE_HAT);
	    }).build();
    
    private static Item register(String nameIn, Item itemIn)
    {
    	ITEMS.put(new Identifier(Reference.ModInfo.MOD_ID, nameIn), itemIn);
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
