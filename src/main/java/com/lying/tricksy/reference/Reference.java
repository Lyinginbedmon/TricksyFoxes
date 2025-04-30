package com.lying.tricksy.reference;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Reference
{
	public static class ModInfo
	{
		public static final String MOD_NAME	= "Tricksy Foxes";
		public static final String MOD_ID	= "tricksy";
		public static final String MOD_PREFIX	= MOD_ID + ":";
		
		public static final String VERSION	= "4.1";
		
		public static final String LEAD_DEV	= "_Lying";
		
		public static Identifier prefix(String name) { return Identifier.of(MOD_ID, name); }
		public static Text translate(String prefix, String suffix) { return Text.translatable(prefix + "." + MOD_ID + "." + suffix); }
		public static Text translate(String prefix, String suffix, Object... args) { return Text.translatable(prefix + "." + MOD_ID + "." + suffix, args); }
	}
	
	public static class Values
	{
		public static final int TICKS_PER_SECOND		= 20;
		public static final int TICKS_PER_MINUTE		= TICKS_PER_SECOND * 60;
		public static final int TICKS_PER_HOUR			= TICKS_PER_MINUTE * 60;
		public static final int ENTITY_MAX_AIR			= 300;
		public static final int TICKS_PER_BUBBLE		= ENTITY_MAX_AIR / TICKS_PER_SECOND;
		public static final int TICKS_PER_DAY			= TICKS_PER_SECOND * 1200;
		public static final double SPEED_OF_SOUND		= 343D;
	}
}