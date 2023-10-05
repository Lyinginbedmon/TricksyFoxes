package com.lying.tricksy.screen;

import java.util.Random;

import com.lying.tricksy.TricksyFoxesClient;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.screen.TreeScreen.HoveredElement;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;

public class ScriptureScreen extends HandledScreen<ScriptureScreenHandler>
{
	private TreeNode<?> hoveredNode = null;
	
	private Vec2f position = Vec2f.ZERO;
	private Vec2f moveStart = null;
	
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
	
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		drawBackground(context, delta, mouseX, mouseY);
		drawForeground(context, mouseX, mouseY);
	}
	
	protected void drawForeground(DrawContext context, int mouseX, int mouseY)
	{
		context.drawText(textRenderer, this.title, (this.width - this.textRenderer.getWidth(this.title)) / 2, 2 + (26 - this.textRenderer.fontHeight) / 2, 0x404040, false);
		if(mouseY < 28 && Math.abs((this.width / 2) - mouseX) < 100)
			hoveredNode = null;
		else
			hoveredNode = isDragging() ? null : getScreenHandler().getRoot().findNodeAt(mouseX, mouseY);
		
		if(hoveredNode != null)
		{
			if(hoveredElement(mouseX, mouseY).isEmpty())
				switch(hoveredNodePart(mouseX, mouseY))
				{
					case SUBTYPE:
						int relativeX = (hoveredNode.screenX + hoveredNode.width / 2) - mouseX;
						if(relativeX > -50 && relativeX < 50)
							context.drawTooltip(textRenderer, hoveredNode.getSubType().description(), mouseX, mouseY);
						break;
					default:
						break;
				}
		}
	}
	
	/** Recursively positions and scales all nodes */
	private void scaleAndPositionNode(TreeNode<?> node, int x, int y)
	{
		node.setPositionAndWidth(x, y, 150, NodeRenderUtils.nodeDisplayHeight(node));
		int childY = node.screenY + node.height + NodeRenderUtils.NODE_SPACING;
		for(TreeNode<?> child : node.children())
		{
			Random childRNG = child.getRNG();
			int xOffset = TricksyFoxesClient.config.fancyTrees() ? childRNG.nextInt(2, 8) * 5 : 10;
			scaleAndPositionNode(child, node.screenX + NodeRenderUtils.CONNECTOR_OFFSET + xOffset, childY);
			childY += NodeRenderUtils.nodeDisplayHeightRecursive(child) + NodeRenderUtils.NODE_SPACING;
		}
	}
	
	private HoveredElement hoveredNodePart(int mouseX, int mouseY)
	{
		if(hoveredNode == null)
			return null;
		
		int yOffset = mouseY - hoveredNode.screenY;
		if(yOffset < 13)
			return HoveredElement.TYPE;
		else if(yOffset < 24)
			return HoveredElement.SUBTYPE;
		else
			return HoveredElement.VARIABLES;
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
		
		scaleAndPositionNode(getScreenHandler().getRoot(), renderX, renderY);
		NodeRenderUtils.renderTree(getScreenHandler().getRoot(), context, this.textRenderer);
		
		NodeRenderUtils.drawTextures(context, (this.width - 200) / 2, 2, 0, 68, 200, 26, 255, 255, 255);
	}
}
