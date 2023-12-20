package com.lying.tricksy.entity;

import java.util.EnumSet;

public interface IAnimatedBiped
{
	public default EnumSet<BipedPart> getPartsAnimating() { return EnumSet.noneOf(BipedPart.class); }
	
	public static enum BipedPart
	{
		HEAD,
		BODY,
		LEFT_ARM,
		RIGHT_ARM,
		LEFT_LEG,
		RIGHT_LEG;
	}
}
