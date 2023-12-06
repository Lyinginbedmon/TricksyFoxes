package com.lying.tricksy.screen;

import com.google.common.base.Predicates;
import com.lying.tricksy.network.ToggleScriptureOverrulePacket;
import com.lying.tricksy.reference.Reference;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;

// TODO Implement translation for overrule button & tooltip

public class ScriptureScreen extends HandledScreen<ScriptureScreenHandler>
{
	private Vec2f position = Vec2f.ZERO;
	private Vec2f moveStart = null;
	
	private int ticksOpen = 0;
	
	private ButtonWidget overrule;
	
	public ScriptureScreen(ScriptureScreenHandler handler, PlayerInventory inventory, Text title)
	{
		super(handler, inventory, title);
	}
	
	protected void init()
	{
		position = new Vec2f(-this.width / 4, -this.height / 4);
		
		addDrawableChild(overrule = ButtonWidget.builder(Text.translatable("gui."+Reference.ModInfo.MOD_ID+".tree_screen.reset"), (button) -> 
		{
			getScreenHandler().toggleOverrule();
			ToggleScriptureOverrulePacket.send(client.player, getScreenHandler().shouldOverrule());
		}).dimensions(this.width - 84, this.height - 20, 80, 16).build());
		overrule.setTooltip(Tooltip.of(Text.literal("Determines the result when a mob is given a scripture it cannot follow")));
	}
	
	public boolean shouldPause() { return true; }
	
	public boolean mouseClicked(double x, double y, int mouseKey)
	{
		if(mouseKey == 0 && !overrule.isMouseOver(x, y))
		{
			this.setDragging(true);
			this.moveStart = new Vec2f((float)x, (float)y);
			return true;
		}
		else
			return super.mouseClicked(x, y, mouseKey);
	}
	
	public boolean mouseReleased(double x, double y, int mouseKey)
	{
		if(mouseKey == 0 && isActuallyDragging())
		{
			float xOff = (float)x - moveStart.x;
			float yOff = (float)y - moveStart.y;
			position = position.add(new Vec2f(xOff, yOff));
			
			this.setDragging(false);
			this.moveStart = null;
			
			return true;
		}
		else
			return super.mouseReleased(x, y, mouseKey);
	}
	
	public void handledScreenTick()
	{
		super.handledScreenTick();
		ticksOpen++;
		
		overrule.setMessage(getScreenHandler().shouldOverrule() ? Text.literal("Permit") : Text.literal("Refuse"));
	}
	
	protected void drawForeground(DrawContext context, int mouseX, int mouseY)
	{
		context.drawText(textRenderer, this.title, (this.width - this.textRenderer.getWidth(this.title)) / 2, 2 + (26 - this.textRenderer.fontHeight) / 2, 0x404040, false);
	}
	
	public boolean isActuallyDragging() { return super.isDragging() && moveStart != null; }
	
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY)
	{
		renderBackground(context);
		int renderX = this.width / 2 + (int)position.x;
		int renderY = this.height / 2 + (int)position.y;
		if(isActuallyDragging())
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
