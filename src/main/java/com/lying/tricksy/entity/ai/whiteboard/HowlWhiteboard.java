package com.lying.tricksy.entity.ai.whiteboard;

import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.init.TFWhiteboards;
import com.lying.tricksy.utility.Howls;
import com.lying.tricksy.utility.Howls.Howl;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;

public class HowlWhiteboard extends InertWhiteboard
{
	public static final WhiteboardRef SENDER = makeSystemRef("sender", TFObjType.ENT, TFWhiteboards.HOWL);
	public static final WhiteboardRef POSITION = makeSystemRef("position", TFObjType.BLOCK, TFWhiteboards.HOWL);
	
	private final LivingEntity tricksy;
	
	public HowlWhiteboard(LivingEntity tricksyIn)
	{
		super(TFWhiteboards.HOWL, tricksyIn.getWorld());
		this.tricksy = tricksyIn;
	}
	
	public Whiteboard<?> build()
	{
		register(SENDER, () -> getHowl().sender);
		register(POSITION, () -> getHowl().position);
		return this;
	}
	
	private Howl getHowl()
	{
		return Howls.getHowls((ServerWorld)tricksy.getWorld()).getCurrentHowl(tricksy.getBlockPos());
	}
}
