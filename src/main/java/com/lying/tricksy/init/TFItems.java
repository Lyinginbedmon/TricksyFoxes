package com.lying.tricksy.init;

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
    public static final Item SAGE_HAT = new ItemSageHat(new FabricItemSettings());
    public static final Item FOX_EGG = new SpawnEggItem(TFEntityTypes.TRICKSY_FOX, 1, 1, new FabricItemSettings());
    
    public static final ItemGroup TRICKSY_GROUP = FabricItemGroup.builder().icon(() -> new ItemStack(SAGE_HAT)).displayName(Text.translatable("itemGroup."+Reference.ModInfo.MOD_ID+".item_group")).build();
    
    public static void init()
    {
		Registry.register(Registries.ITEM_GROUP, new Identifier(Reference.ModInfo.MOD_ID, "item_group"), TRICKSY_GROUP);
		ItemGroupEvents.modifyEntriesEvent(Registries.ITEM_GROUP.getKey(TRICKSY_GROUP).get()).register((content) -> 
		{
			content.add(SAGE_HAT);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register((content) -> 
		{
			content.add(FOX_EGG);
		});
		
		Registry.register(Registries.ITEM, new Identifier(Reference.ModInfo.MOD_ID, "sage_hat"), SAGE_HAT);
		Registry.register(Registries.ITEM, new Identifier(Reference.ModInfo.MOD_ID, "fox_spawn_egg"), FOX_EGG);
    }
}
