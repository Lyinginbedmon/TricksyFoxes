package com.lying.tricksy.screen.subscreen.dialog;

import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.screen.subscreen.CreateStaticScreen;
import com.lying.tricksy.screen.subscreen.NestedScreen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public abstract class ValueDialog<T> extends NestedScreen<CreateStaticScreen>
{
	public static final Identifier DIALOG_TEXTURES = new Identifier(Reference.ModInfo.MOD_ID, "textures/gui/dialogs.png");
	
	public ValueDialog(CreateStaticScreen parentIn)
	{
		super(parentIn);
	}
	
	public abstract IWhiteboardObject<T> createValue();
	
	protected void renderTitle(Text title, DrawContext context, int y)
	{
		context.drawText(client.textRenderer, title, (this.width - client.textRenderer.getWidth(title)) / 2, y, 0x505050, false);
	}
	
	protected final TextFieldWidget makeCentredIntInput(int x, int y, int initialVal, boolean allowNegative)
	{
		NumberFieldWidget input = new NumberFieldWidget(this.textRenderer, x - (70 / 2), y);
		prepareTextField(input);
		input.setText(String.valueOf(initialVal));
		input.setAllowNegative(allowNegative);
		return input;
	}
	
	private static void prepareTextField(TextFieldWidget input)
	{
		input.setEditableColor(-1);
		input.setUneditableColor(-1);
		input.setDrawsBackground(false);
		input.setMaxLength(10);
		input.setFocusUnlocked(true);
		input.setEditable(true);
	}
	
	/** TextFieldWidget class that only accepts numerical input */
	private static class NumberFieldWidget extends TextFieldWidget
	{
		protected boolean drawsBackground = false;
		private boolean negativeAllowed = true;
		
		public NumberFieldWidget(TextRenderer textRenderer, int x, int y)
		{
			super(textRenderer, x, y, 70, 20, Text.empty());
		}
		
		public void setAllowNegative(boolean bool) { this.negativeAllowed = bool; }
		
		public void setDrawsBackground(boolean drawsBackground)
		{
			super.setDrawsBackground(drawsBackground);
			this.drawsBackground = drawsBackground;
		}
		
		private boolean drawsBackground()
		{
			return this.drawsBackground;
		}
		
		public boolean charTyped(char chr, int modifiers)
		{
			if(getText().length() == 0 && (chr == '+' || chr == '-' && negativeAllowed) || chr >= '0' && chr <= '9')
				return super.charTyped(chr, modifiers);
			return false;
		}
		
		public void renderButton(DrawContext context, int mouseX, int mouseY, float delta)
		{
			if(!drawsBackground() && isVisible())
				context.drawTexture(DIALOG_TEXTURES, getX() - 5, getY() - 6, 0, 26, width + 2, height + 2);
			super.renderButton(context, mouseX, mouseY, delta);
		}
	}
}
