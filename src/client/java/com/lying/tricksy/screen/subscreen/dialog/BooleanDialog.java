package com.lying.tricksy.screen.subscreen.dialog;

import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.reference.Reference;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class BooleanDialog extends ValueDialog<Boolean>
{
	private boolean value = false;
	
	protected void init()
	{
		clearChildren();
		addDrawableChild(ButtonWidget.builder(Text.translatable("value."+Reference.ModInfo.MOD_ID+".boolean.false"), (button) -> 
		{
			value = !value;
			button.setMessage(Text.translatable("value."+Reference.ModInfo.MOD_ID+".boolean."+(value ? "true" : "false")));
		}).dimensions(this.width / 2 - 20, this.height / 2 - 20, 40, 40).build());
	}
	
	public IWhiteboardObject<Boolean> createValue()
	{
		return new WhiteboardObj.Bool(value);
	}
}
