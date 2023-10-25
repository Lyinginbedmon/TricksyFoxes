package com.lying.tricksy.screen.subscreen.dialog;

import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class IntegerDialog extends ValueDialog<Integer>
{
	private TextFieldWidget input;
	
	protected void init()
	{
		addDrawableChild(input = new TextFieldWidget(this.textRenderer, this.width / 2 - 52, this.height / 2 - 10, 104, 20, Text.translatable("container.repair")));
		addSelectableChild(input);
		setInitialFocus(input);
	}
	
	public void tick()
	{
		input.tick();
	}
	
	public IWhiteboardObject<Integer> createValue()
	{
		int val = 0;
		try
		{
			val = Integer.valueOf(input.getText());
		}
		catch(NumberFormatException e) { }
		return new WhiteboardObj.Int(val);
	}
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if(this.input.keyPressed(keyCode, scanCode, modifiers))
			return true;
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}
