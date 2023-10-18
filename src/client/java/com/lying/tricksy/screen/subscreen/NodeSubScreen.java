package com.lying.tricksy.screen.subscreen;

import com.lying.tricksy.screen.NodeScreen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class NodeSubScreen extends Screen
{
	protected final NodeScreen parent;
	
	public NodeSubScreen(NodeScreen parentIn)
	{
		super(Text.empty());
		parent = parentIn;
	}
}
