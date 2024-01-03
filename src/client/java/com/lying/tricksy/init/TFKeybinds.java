package com.lying.tricksy.init;

import com.lying.tricksy.reference.Reference;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class TFKeybinds
{
	public static KeyBinding keyIncOrder;
	public static KeyBinding keyDecOrder;
	
	public static KeyBinding make(String name, InputUtil.Type type, int standard)
	{
		return KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key."+Reference.ModInfo.MOD_ID+"."+name,
				type,
				standard,
				"category."+Reference.ModInfo.MOD_ID+".keybindings"));
	}
}
