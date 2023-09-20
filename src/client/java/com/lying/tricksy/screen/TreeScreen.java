package com.lying.tricksy.screen;

import java.util.UUID;

import com.lying.tricksy.entity.ai.node.NodeSubType;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFNodeTypes;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;

public class TreeScreen extends HandledScreen<TreeScreenHandler>
{
	private static final int NODE_WIDTH = 150;
	
	private TreeNode<?> hoveredNode = null;
	private HoveredElement hoveredPart = null;
	
	// Button to add a new node
	public ButtonWidget addNode;
	// Button to delete a node
	public ButtonWidget delNode;
	
	public ButtonWidget reset;
	public ButtonWidget save;
	
	private Vec2f position = Vec2f.ZERO;
	private Vec2f moveStart = null;
	
	public TreeScreen(TreeScreenHandler handler, PlayerInventory playerInventory, Text title)
	{
		super(handler, playerInventory, title);
	}
	
	protected void init()
	{
		addDrawableChild(addNode = ButtonWidget.builder(Text.literal("+"), (button) -> 
		{
			hoveredNode.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID()));
		}).dimensions(16, 16, 16, 16).build());
		addDrawableChild(delNode = ButtonWidget.builder(Text.literal("-"), (button) -> 
		{
			handler.getTree().root().removeChild(hoveredNode);
		}).dimensions(16, 32, 16, 16).build());
		addDrawableChild(reset = ButtonWidget.builder(Text.literal("Reset"), (button) -> 
		{
			handler.resetTree();
		}).dimensions(16, 16, 32, 16).build());
		addDrawableChild(save = ButtonWidget.builder(Text.literal("Save"), (button) -> 
		{
			handler.setWrite(true);
			client.setScreen(null);
		}).dimensions(this.width - 48, 16, 32, 16).build());
		
		position = new Vec2f(-this.width / 4, -this.height / 4);
	}
	
	public boolean shouldPause() { return true; }
	
	public boolean mouseClicked(double x, double y, int mouseKey)
	{
		if(mouseKey == 0)
		{
			for(Element widget : children())
			{
				if(!widget.isMouseOver(x, y) || !(widget instanceof ClickableWidget))
					continue;
				return widget.mouseClicked(x, y, mouseKey);
			}
			
			this.setDragging(true);
			this.moveStart = new Vec2f((float)x, (float)y);
			return true;
		}
		return super.mouseClicked(x, y, mouseKey);
	}
	
	public boolean mouseScrolled(double mouseX, double mouseY, double amount)
	{
		if(hoveredNode != null)
		{
			switch(hoveredPart)
			{
				case SUBTYPE:
					hoveredNode.changeSubType((int)amount);
					break;
				case TYPE:
					
					break;
				case VARIABLES:
					break;
				default:
					break;
			}
		}
		return super.mouseScrolled(mouseX, mouseY, amount);
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
	
	protected void drawForeground(DrawContext context, int mouseX, int mouseY)
	{
		context.drawCenteredTextWithShadow(textRenderer, this.title, this.width / 2, 16, -1);
		TreeNode<?> root = handler.getTree().root();
		hoveredNode = isDragging() ? null : root.findNodeAt(mouseX, mouseY);
		
		if(hoveredNode != null && hoveredElement(mouseX, mouseY).isEmpty())
		{
			addNode.visible = delNode.visible = true;
			addNode.active = hoveredNode.canAddChild(null);
			delNode.active = hoveredNode != root;
			addNode.setPosition(hoveredNode.screenX + hoveredNode.width - 4 - addNode.getWidth(), hoveredNode.screenY);
			delNode.setPosition(hoveredNode.screenX, hoveredNode.screenY);
			
			switch(hoveredPart = hoveredNodePart(mouseX, mouseY))
			{
				case SUBTYPE:
					context.drawTooltip(textRenderer, hoveredNode.getSubType().description(), mouseX, mouseY);
					break;
				case TYPE:
					break;
				case VARIABLES:
					break;
				default:
					break;
			}
		}
		else
		{
			hoveredPart = null;
			addNode.visible = addNode.active = false;
			delNode.visible = delNode.active = false;
		}
	}
	
	private void renderNode(TreeNode<?> node, int x, int y, DrawContext context)
	{
		node.setPositionAndWidth(x, y, NODE_WIDTH, nodeDisplayHeight(node));
		
		drawNodeBackground(context, node, node.getType().color(), x, y);
//		context.fill(x - 1, y - 1, x + NODE_WIDTH, y + nodeDisplayHeight(node), node.getType().color());
		
		Text typeName = node.getType().translatedName();
		context.drawText(this.textRenderer, typeName, x + (NODE_WIDTH - this.textRenderer.getWidth(typeName)) / 2, y, 0x404040, false);
		
		NodeSubType<?> subType = node.getSubType();
		Text subName = subType.translatedName();
		context.drawText(this.textRenderer, subName, x + (NODE_WIDTH - this.textRenderer.getWidth(subName)) / 2, y + this.textRenderer.fontHeight + 2, 0x404040, false);
		int varY = y + 2 + this.textRenderer.fontHeight * 2;
		for(WhiteboardRef input : subType.variableSet().keySet())
		{
			context.drawText(this.textRenderer, input.displayName(), x, varY, 0x404040, false);
			
			WhiteboardRef variable = node.variable(input);
			if(variable != null)
				context.drawText(this.textRenderer, variable.displayName(), x + (NODE_WIDTH / 2), varY, 0x404040, false);
			varY += 2 + this.textRenderer.fontHeight;
		};
		
		for(TreeNode<?> child : node.children())
		{
			renderNode(child, x + 30, varY + 4, context);
			varY += nodeDisplayHeightRecursive(child);
		}
	}
	
	private HoveredElement hoveredNodePart(int mouseX, int mouseY)
	{
		if(hoveredNode == null)
			return null;
		
		int yOffset = mouseY - hoveredNode.screenY;
		if(yOffset < this.textRenderer.fontHeight)
			return HoveredElement.TYPE;
		else if(yOffset < (this.textRenderer.fontHeight * 2))
			return HoveredElement.SUBTYPE;
		else
			return HoveredElement.VARIABLES;
	}
	
	private void drawNodeBackground(DrawContext context, TreeNode<?> node, int colour, int x, int y)
	{
		int minX = x - 2;
		int maxX = minX + node.width + 2;
		int minY = y - 2;
		int maxY = minY + nodeDisplayHeight(node);
		
		// Main backing
		context.fill(minX, minY, maxX, maxY, -1);
		
//		Tessellator tessellator = Tessellator.getInstance();
//		Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
//		BufferBuilder buffer = tessellator.getBuffer();
//		buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
//			buffer.vertex(matrix, minX, minY, 0).color(0, 0.5F, 1F, 1F);
//			buffer.vertex(matrix, maxX, minY, 0).color(0, 0.5F, 1F, 1F);
//			buffer.vertex(matrix, maxX, maxY, 0).color(0, 0.5F, 1F, 1F);
//			buffer.vertex(matrix, minX, maxY, 0).color(0, 0.5F, 1F, 1F);
//		tessellator.draw();
	}
	
	private void drawNodeConnections(DrawContext context, TreeNode<?> node, int colour)
	{
		if(node.children().isEmpty())
			return;
		
		int lineWidth = 2;
		
		int startX = node.screenX + 5;
		int endX = startX + lineWidth;
		int startY = node.screenY + node.height - 5;
		for(TreeNode<?> child : node.children())
		{
			int endY = child.screenY + (child.height / 2);
			context.fill(startX, startY, endX, endY, -1);
			context.fill(startX, endY - lineWidth, child.screenX - 5, endY, -1);
			
			drawNodeConnections(context, child, child.getType().color());
		}
	}
	
	private int nodeDisplayHeightRecursive(TreeNode<?> nodeIn)
	{
		int height = 0;
		for(TreeNode<?> child : nodeIn.children())
			height += nodeDisplayHeightRecursive(child);
		
		return height + nodeDisplayHeight(nodeIn) + 2;
	}
	
	private int nodeDisplayHeight(TreeNode<?> nodeIn)
	{
		int height = this.textRenderer.fontHeight * 2 + 4;
		int inputs = nodeIn.getSubType().variableSet().keySet().size();
		
		return height + inputs * (this.textRenderer.fontHeight + 1);
	}
	
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY)
	{
		renderBackgroundTexture(context);
		if(handler.getTree() == null)
			return;
		
		TreeNode<?> root = handler.getTree().root();
		int renderX = this.width / 2 + (int)position.x;
		int renderY = this.height / 2 + (int)position.y;
		if(isDragging())
		{
			int offsetX = mouseX - (int)moveStart.x;
			int offsetY = mouseY - (int)moveStart.y;
			
			renderX += offsetX;
			renderY += offsetY;
		}
		
		// Recursively render all nodes
		renderNode(root, renderX, renderY, context);
		drawNodeConnections(context, root, root.getType().color());
	}
	
	private static enum HoveredElement
	{
		TYPE,
		SUBTYPE,
		VARIABLES;
	}
}
