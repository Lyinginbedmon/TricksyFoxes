package com.lying.tricksy.component;

import net.minecraft.util.Identifier;

public class Accomplishment
{
	private final Identifier name;
	
	public Accomplishment(Identifier nameIn)
	{
		this.name = nameIn;
	}
	
	public final Identifier registryName() { return name; }
}
