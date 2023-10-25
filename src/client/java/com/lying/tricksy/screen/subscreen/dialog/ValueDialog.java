package com.lying.tricksy.screen.subscreen.dialog;

import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class ValueDialog<T> extends Screen
{
	public ValueDialog()
	{
		super(Text.empty());
	}
	
	public abstract IWhiteboardObject<T> createValue();
}
