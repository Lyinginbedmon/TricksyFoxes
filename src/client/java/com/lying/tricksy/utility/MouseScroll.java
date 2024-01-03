package com.lying.tricksy.utility;

import net.minecraft.client.MinecraftClient;

@FunctionalInterface
public interface MouseScroll
{
	/** Returns true if a given instance of scrolling was handled by this method instead of standard handling */
	public boolean onMouseScroll(MinecraftClient client, double vertical, double horizontal);
}
