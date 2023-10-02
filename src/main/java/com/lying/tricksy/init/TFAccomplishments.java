package com.lying.tricksy.init;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.tricksy.component.Accomplishment;
import com.lying.tricksy.reference.Reference;

import net.minecraft.util.Identifier;

public class TFAccomplishments
{
	private static final List<Accomplishment> ACCOMPLISHMENTS = Lists.newArrayList();
	
	public static final Accomplishment VISIT_NETHER = make("visit_nether");
	public static final Accomplishment VISIT_OVERWORLD = make("visit_overworld");
	public static final Accomplishment VISIT_END = make("visit_end");
	public static final Accomplishment DIMENSIONAL_TRAVEL = make("dimensional_travel");
	
	private static Accomplishment make(String nameIn)
	{
		Accomplishment made = new Accomplishment(new Identifier(Reference.ModInfo.MOD_ID, nameIn));
		ACCOMPLISHMENTS.add(made);
		return made;
	}
	
	public static Accomplishment get(Identifier nameIn)
	{
		for(Accomplishment acc : ACCOMPLISHMENTS)
			if(acc.registryName().equals(nameIn))
				return acc;
		return null;
	}
}
