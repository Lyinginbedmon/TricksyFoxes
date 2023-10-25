package com.lying.tricksy.screen.subscreen.dialog;

import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public abstract class ValueDialog<T> extends Screen
{
	public ValueDialog()
	{
		super(Text.empty());
	}
	
	public abstract IWhiteboardObject<T> createValue();
	
	protected final TextFieldWidget makeCentredTextInput(int x, int y, int width, String initialText)
	{
		TextFieldWidget input = new TextFieldWidget(this.textRenderer, x - (width / 2), y, width, 20, Text.empty());
		prepareTextField(input);
		input.setText(initialText);
		return input;
	}
	
	protected final TextFieldWidget makeCentredIntInput(int x, int y, int width, int initialVal, boolean allowNegative)
	{
		NumberFieldWidget input = new NumberFieldWidget(this.textRenderer, x - (width / 2), y, width, 20);
		prepareTextField(input);
		input.setText(String.valueOf(initialVal));
		input.setAllowNegative(allowNegative);
		return input;
	}
	
	private static void prepareTextField(TextFieldWidget input)
	{
		input.setEditableColor(-1);
		input.setUneditableColor(-1);
		input.setDrawsBackground(true);
		input.setMaxLength(10);
		input.setFocusUnlocked(true);
		input.setEditable(true);
	}
	
	/** TextFieldWidget class that only accepts numerical input */
	private static class NumberFieldWidget extends TextFieldWidget
	{
		private boolean negativeAllowed = true;
		
		public NumberFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height)
		{
			super(textRenderer, x, y, width, height, Text.empty());
		}
		
		public void setAllowNegative(boolean bool) { this.negativeAllowed = bool; }
		
		public boolean charTyped(char chr, int modifiers)
		{
			if(getText().length() == 0 && (chr == '+' || chr == '-' && negativeAllowed) || chr >= '0' && chr <= '9')
				return super.charTyped(chr, modifiers);
			return false;
		}
	}
}
