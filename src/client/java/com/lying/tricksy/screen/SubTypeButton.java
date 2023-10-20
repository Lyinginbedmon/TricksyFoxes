package com.lying.tricksy.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;

public class SubTypeButton extends TexturedButtonWidget
{
	public static final MinecraftClient mc = MinecraftClient.getInstance();
	private final Text message;
	
	public SubTypeButton(int x, int y, Text message, PressAction pressAction)
	{
		super(x, y, 148, 20, 20, 0, NodeScreen.EDITOR_TEXTURES, pressAction);
		this.message = message;
	}
	
	public Text getMessage() { return this.message; }
	
	public void renderButton(DrawContext context, int mouseX, int mouseY, float delta)
	{
		super.renderButton(context, mouseX, mouseY, delta);
		context.drawText(
				mc.textRenderer, 
				getMessage(), 
				getX() + (getWidth() - mc.textRenderer.getWidth(getMessage())) / 2, 
				getY() + (getHeight() - mc.textRenderer.fontHeight) / 2 + 1, 
				this.active ? 0x505050 : 0x909090, 
						false);
	}
}