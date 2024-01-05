package com.lying.tricksy.integration;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Function;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.screen.PrescientCandleScreen;
import com.lying.tricksy.screen.TricksyScreenBase;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import net.minecraft.client.gui.screen.Screen;

public class REICompat implements REIClientPlugin
{
	public String getPluginProviderName() { return Reference.ModInfo.MOD_ID; }
	
	public void registerExclusionZones(ExclusionZones zones)
	{
		Function<Screen,Rectangle> wholeScreen = screen -> new Rectangle(0, 0, screen.width, screen.height);
		zones.register(TricksyScreenBase.class, screen -> 
		{
			return screen != null ? List.of(wholeScreen.apply(screen)) : Collections.emptyList();
		});
		zones.register(PrescientCandleScreen.class, screen -> 
		{
			return screen != null ? List.of(wholeScreen.apply(screen)) : Collections.emptyList();
		});
	}
}
