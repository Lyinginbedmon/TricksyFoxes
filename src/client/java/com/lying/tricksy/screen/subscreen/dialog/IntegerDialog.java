package com.lying.tricksy.screen.subscreen.dialog;

import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class IntegerDialog extends ValueDialog<Integer>
{
	public static final int[] BUTTON_VALS = new int[]{-20, -10, -1, 1, 10, 20};
	private TextFieldWidget input;
	
	protected void init()
	{
		addSelectableChild(input = makeCentredIntInput(this.width / 2, this.height / 2 - 10, 70, 4, false));
		setInitialFocus(input);
		
		int y = (this.height / 2) + 30;
		int x = (this.width - BUTTON_VALS.length * 30) / 2;
		for(int val : BUTTON_VALS)
		{
			addDrawableChild(ButtonWidget.builder(Text.literal((val >= 0 ? "+" : "")+String.valueOf(val)), (button) -> incVal(val * (hasShiftDown() ? 10 : 1))).dimensions(x, y, 25, 20).build());
			x += 30;
		}
	}
	
	public void incVal(int amount)
	{
		int val = Integer.valueOf(input.getText());
		val = Math.max(0, val + amount);
		input.setText(String.valueOf(val));
	}
	
	public void tick()
	{
		super.tick();
		input.tick();
	}
	
	public IWhiteboardObject<Integer> createValue()
	{
		int val = 0;
		try
		{
			val = Math.max(0, Integer.valueOf(input.getText()));
		}
		catch(NumberFormatException e) { }
		return new WhiteboardObj.Int(val);
	}
	
	public boolean mouseScrolled(double mouseX, double mouseY, double amount)
	{
		if(this.input.isFocused())
		{
			incVal((int)(amount * (hasShiftDown() ? 10 : 1)));
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, amount);
	}
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if(this.input.keyPressed(keyCode, scanCode, modifiers))
			return true;
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		super.render(context, mouseX, mouseY, delta);
		input.render(context, mouseX, mouseY, delta);
	}
}
