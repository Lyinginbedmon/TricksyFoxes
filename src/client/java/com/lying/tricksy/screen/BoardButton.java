package com.lying.tricksy.screen;

import com.lying.tricksy.init.TFWhiteboards.BoardType;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;

public class BoardButton extends TexturedButtonWidget
{
	public static final MinecraftClient mc = MinecraftClient.getInstance();
	private final Text message;
	
	public BoardButton(int x, int y, BoardType board)
	{
		this(x, y, board.translate(), (button) -> 
		{
			WhiteboardScreen screen = (WhiteboardScreen)WhiteboardScreen.mc.currentScreen;
			screen.setBoard(board);
			screen.manageBoardButtons();
		});
	}
	
	public BoardButton(int x, int y, Text message, PressAction pressAction)
	{
		super(x, y, 60, 20, 0, 25, WhiteboardList.BOARD_TEXTURES, pressAction);
		this.message = message;
	}
	
	public void renderButton(DrawContext context, int mouseX, int mouseY, float delta)
	{
		super.renderButton(context, mouseX, mouseY, delta);
		if(this.message.getString().length() == 0)
			return;
		int color = this.active ? 0x505050 : 0x909090;
		context.drawText(
				WhiteboardScreen.mc.textRenderer, 
				message, 
				this.getX() + (this.active ? 3 : 0) + (this.getWidth() - mc.textRenderer.getWidth(message)) / 2, 
				this.getY() + (getHeight() - mc.textRenderer.fontHeight) / 2 + 1, 
				color, 
				false);
	}
}