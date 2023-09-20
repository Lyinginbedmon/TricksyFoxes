package com.lying.tricksy.screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.joml.Matrix4f;

import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ai.node.NodeSubType;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.reference.Reference;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

public class TreeScreen extends HandledScreen<TreeScreenHandler>
{
	public static final Identifier TREE_TEXTURES = new Identifier(Reference.ModInfo.MOD_ID, "textures/gui/behaviour_tree.png");
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
		}).dimensions(this.width / 2 - 100 + 10, 7, 40, 16).build());
		addDrawableChild(save = ButtonWidget.builder(Text.literal("Save"), (button) -> 
		{
			handler.setWrite(true);
			client.setScreen(null);
		}).dimensions(this.width / 2 + 100 - 40 - 10, 7, 40, 16).build());
		
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
					// Cycle node types
					break;
				case VARIABLES:
					// Cycle available input values
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
		context.drawText(textRenderer, this.title, (this.width - this.textRenderer.getWidth(this.title)) / 2, 2 + (26 - this.textRenderer.fontHeight) / 2, 0x404040, false);
		TreeNode<?> root = handler.getTree().root();
		if(mouseY < 28 && Math.abs((this.width / 2) - mouseX) < 100)
			hoveredNode = null;
		else
			hoveredNode = isDragging() ? null : root.findNodeAt(mouseX, mouseY);
		
		if(hoveredNode != null)
		{
			addNode.visible = delNode.visible = true;
			addNode.active = hoveredNode.canAddChild(null);
			delNode.active = hoveredNode != root;
			addNode.setPosition(hoveredNode.screenX + hoveredNode.width - 7 - addNode.getWidth(), hoveredNode.screenY + 6);
			delNode.setPosition(hoveredNode.screenX + 7, hoveredNode.screenY + 6);
			
			if(hoveredElement(mouseX, mouseY).isEmpty())
				switch(hoveredPart = hoveredNodePart(mouseX, mouseY))
				{
					case SUBTYPE:
						int relativeX = (hoveredNode.screenX + hoveredNode.width / 2) - mouseX;
						if(relativeX > -50 && relativeX < 50)
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
	
	/** Recursively positions and scales all nodes */
	private void scaleAndPositionNode(TreeNode<?> node, int x, int y)
	{
		node.setPositionAndWidth(x, y, 150, nodeDisplayHeight(node));
		int childY = node.screenY + node.height + 4;
		for(TreeNode<?> child : node.children())
		{
			scaleAndPositionNode(child, x + 30, childY);
			childY += nodeDisplayHeightRecursive(child);
		}
	}
	
	private void renderNode(TreeNode<?> node, DrawContext context)
	{
		drawNodeBackground(context, node, node.getType().color(), node.screenX, node.screenY);
		
		int drawY = node.screenY + 4;
		Text typeName = node.getType().translatedName();
		context.drawText(this.textRenderer, typeName, node.screenX + (NODE_WIDTH - this.textRenderer.getWidth(typeName)) / 2, drawY, -1, false);
		drawY += 11;
		
		NodeSubType<?> subType = node.getSubType();
		Text subName = subType.translatedName();
		context.drawText(this.textRenderer, subName, node.screenX + (NODE_WIDTH - this.textRenderer.getWidth(subName)) / 2, drawY, -1, false);
		drawY += 11;
		
		List<Pair<Text, Optional<Text>>> variablesToDisplay = Lists.newArrayList();
		for(WhiteboardRef input : subType.variableSet().keySet())
		{
			Text inputName = input.displayName();
			
			Optional<Text> variableName = Optional.empty();
			WhiteboardRef variable = node.variable(input);
			if(variable != null)
				variableName = Optional.of(variable.displayName());
			variablesToDisplay.add(new Pair<>(inputName, variableName));
		}
		// Sort variables by input name
		variablesToDisplay.sort(new Comparator<>()
		{
			public int compare(Pair<Text, Optional<Text>> o1, Pair<Text, Optional<Text>> o2)
			{
				Text a = o1.getLeft();
				Text b = o2.getLeft();
				ArrayList<String> names = Lists.newArrayList();
				names.add(a.getString());
				names.add(b.getString());
				Collections.sort(names);
				
				int indA = names.indexOf(a.getString());
				int indB = names.indexOf(b.getString());
				return indA > indB ? 1 : indA < indB ? -1 : 0;
			}
		});
		
		for(Pair<Text, Optional<Text>> line : variablesToDisplay)
		{
			context.drawText(this.textRenderer, line.getLeft(), node.screenX + 4 + (46 - this.textRenderer.getWidth(line.getLeft())) / 2, drawY, 0x404040, false);
			if(line.getRight().isPresent())
				context.drawText(this.textRenderer, line.getRight().get(), node.screenX + 50 + (100 - this.textRenderer.getWidth(line.getRight().get())) / 2, drawY, 0x404040, false);
			drawY += 11;
		}
		
		for(TreeNode<?> child : node.children())
			renderNode(child, context);
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
	
	private void drawNodeBackground(DrawContext context, TreeNode<?> node, int colour, int x, int y)
	{
        int r = ((colour & 0xFF0000) >> 16);
        int g = ((colour & 0xFF00) >> 8);
        int b = ((colour & 0xFF) >> 0);
		
		// Type
		int drawY = y;
		context.drawTexture(TREE_TEXTURES, x, drawY, 0, 0, 150, 13);
		drawTintedTexture(TREE_TEXTURES, context, x, drawY, 0, 0, 150, 13, r, g, b);
		drawY += 13;
		
		// Subtype
		int variables = node.getSubType().variableSet().size();
		drawTintedTexture(TREE_TEXTURES, context, x, drawY, 0, variables == 0 ? 26 : 14, 150, variables == 0 ? 15 : 11, r, g, b);
		context.drawTexture(TREE_TEXTURES, x + 25, drawY, 150, 14, 100, 11);
		drawY += 11;
		
		// Variables
		if(variables > 0)
			for(int i=0; i<variables; i++)
			{
				boolean isLast = i == variables - 1;
				context.drawTexture(TREE_TEXTURES, x, drawY, 0, isLast ? 53 : 41, 150, isLast ? 14 : 11);
				drawY += 11;
			}
		
		if(!node.children().isEmpty())
			context.drawTexture(TREE_TEXTURES, x + 2, y + node.height - 5, 150, 0, 10, 10);
	}
	
	private void drawNodeConnections(DrawContext context, TreeNode<?> node, int colour)
	{
		if(node.children().isEmpty())
			return;
		
		int lineWidth = 2;
		
		int startX = node.screenX + 6;
		int endX = startX + lineWidth;
		int startY = node.screenY + node.height;
		for(TreeNode<?> child : node.children())
		{
			int endY = child.screenY + (child.height / 2);
			context.fill(startX, startY, endX, endY, decimalToARGB(colour));
			context.fill(startX, endY - lineWidth, child.screenX - 2, endY, decimalToARGB(colour));
			
			drawNodeConnections(context, child, child.getType().color());
		}
	}
	
	private int nodeDisplayHeightRecursive(TreeNode<?> nodeIn)
	{
		int height = nodeDisplayHeight(nodeIn);
		int childNodes = nodeIn.children().size();
		for(TreeNode<?> child : nodeIn.children())
			height += nodeDisplayHeightRecursive(child);
		return height + (4 * childNodes - 1) + 2;
	}
	
	private int nodeDisplayHeight(TreeNode<?> nodeIn)
	{
		int variables = nodeIn.getSubType().variableSet().size();
		return (13) + (variables == 0 ? 15 : 11) + ((11 * variables - 1) + (variables == 0 ? 0 : 3));
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
		
		scaleAndPositionNode(root, renderX, renderY);
		drawNodeConnections(context, root, root.getType().color());
		renderNode(root, context);
		
		context.drawTexture(TREE_TEXTURES, (this.width - 200) / 2, 2, 0, 68, 200, 26);
	}
	
	private static int decimalToARGB(int colour)
	{
        float r = ((colour & 0xFF0000) >> 16) / 255F;
        float g = ((colour & 0xFF00) >> 8) / 255F;
        float b = ((colour & 0xFF) >> 0) / 255F;
		return MathHelper.packRgb(r, g, b) | 255 << 24;
	}
	
	protected void drawTintedTexture(Identifier texture, DrawContext context, int x, int y, int uv0, int uv1, int width, int height, int red, int green, int blue)
	{
		int x1 = x;
		int x2 = x + width;
		int y1 = y;
		int y2 = y + height;
		
		float u1 = (float)uv0 / 256F;
		float u2 = (float)(uv0 + width) / 256F;
		float v1 = (float)uv1 / 256F;
		float v2 = (float)(uv1 + height) / 256F;
		
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
        RenderSystem.enableBlend();
        Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
        bufferBuilder.vertex(matrix4f, x1, y1, 0).color(red, green, blue, 255).texture(u1, v1).next();
        bufferBuilder.vertex(matrix4f, x1, y2, 0).color(red, green, blue, 255).texture(u1, v2).next();
        bufferBuilder.vertex(matrix4f, x2, y2, 0).color(red, green, blue, 255).texture(u2, v2).next();
        bufferBuilder.vertex(matrix4f, x2, y1, 0).color(red, green, blue, 255).texture(u2, v1).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
	}
	
	private static enum HoveredElement
	{
		TYPE,
		SUBTYPE,
		VARIABLES;
	}
}
