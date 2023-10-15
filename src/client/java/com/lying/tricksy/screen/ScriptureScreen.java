package com.lying.tricksy.screen;

import com.google.common.base.Predicates;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;

public class ScriptureScreen extends HandledScreen<ScriptureScreenHandler>
{
	private Vec2f position = Vec2f.ZERO;
	private Vec2f moveStart = null;
	
	private int ticksOpen = 0;
	
	public ScriptureScreen(ScriptureScreenHandler handler, PlayerInventory inventory, Text title)
	{
		super(handler, inventory, title);
	}
	
	protected void init()
	{
		position = new Vec2f(-this.width / 4, -this.height / 4);
	}
	
	public boolean shouldPause() { return true; }
	
	public boolean mouseClicked(double x, double y, int mouseKey)
	{
		if(mouseKey == 0)
		{
			this.setDragging(true);
			this.moveStart = new Vec2f((float)x, (float)y);
			return true;
		}
		return super.mouseClicked(x, y, mouseKey);
	}
	
	public boolean mouseReleased(double x, double y, int mouseKey)
	{
		if(mouseKey == 0 && isDragging())
		{
			float xOff = (float)x - moveStart.x;
			float yOff = (float)y - moveStart.y;
			position = position.add(new Vec2f(xOff, yOff));
			
			this.setDragging(false);
			this.moveStart = null;
			
			return true;
		}
		return super.mouseReleased(mouseKey, mouseKey, mouseKey);
	}
	
	public void handledScreenTick()
	{
		ticksOpen++;
	}
	
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		drawBackground(context, delta, mouseX, mouseY);
		drawForeground(context, mouseX, mouseY);
	}
	
	protected void drawForeground(DrawContext context, int mouseX, int mouseY)
	{
		context.drawText(textRenderer, this.title, (this.width - this.textRenderer.getWidth(this.title)) / 2, 2 + (26 - this.textRenderer.fontHeight) / 2, 0x404040, false);
	}
	
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY)
	{
		renderBackground(context);
		int renderX = this.width / 2 + (int)position.x;
		int renderY = this.height / 2 + (int)position.y;
		if(isDragging())
		{
			int offsetX = mouseX - (int)moveStart.x;
			int offsetY = mouseY - (int)moveStart.y;
			
			renderX += offsetX;
			renderY += offsetY;
		}
		
		NodeRenderUtils.scaleAndPositionNode(getScreenHandler().getRoot(), renderX, renderY, Predicates.alwaysTrue(), false);
		NodeRenderUtils.renderTree(getScreenHandler().getRoot(), context, this.textRenderer, this.ticksOpen, Predicates.alwaysTrue(), false);
		
		NodeRenderUtils.drawTextures(context, (this.width - 200) / 2, 2, 0, 68, 200, 26, 255, 255, 255);
	}
}
