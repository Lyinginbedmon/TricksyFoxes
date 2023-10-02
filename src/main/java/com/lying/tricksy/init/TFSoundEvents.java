package com.lying.tricksy.init;

import com.lying.tricksy.reference.Reference;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class TFSoundEvents
{
	private static final Identifier ID_WHITEBOARD_UPDATED = new Identifier(Reference.ModInfo.MOD_ID, "whiteboard_updated");
	public static final SoundEvent WHITEBOARD_UPDATED = SoundEvent.of(ID_WHITEBOARD_UPDATED);
	
	private static final Identifier ID_TRICKSY_ENLIGHTENED = new Identifier(Reference.ModInfo.MOD_ID, "tricksy_enlightened");
	public static final SoundEvent TRICKSY_ENLIGHTENED = SoundEvent.of(ID_TRICKSY_ENLIGHTENED);
	
	public static void init()
	{
		Registry.register(Registries.SOUND_EVENT, ID_WHITEBOARD_UPDATED, WHITEBOARD_UPDATED);
		Registry.register(Registries.SOUND_EVENT, ID_TRICKSY_ENLIGHTENED, TRICKSY_ENLIGHTENED);
	}
}
