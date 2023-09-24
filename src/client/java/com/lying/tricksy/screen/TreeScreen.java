package com.lying.tricksy.screen;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

import com.lying.tricksy.TricksyFoxesClient;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.network.SaveTreePacket;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec2f;

@Environment(EnvType.CLIENT)
public class TreeScreen extends HandledScreen<TreeScreenHandler>
{
	
	private final PlayerEntity player;
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
		this.player = playerInventory.player;
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
		
		int midPoint = this.width / 2;
		addDrawableChild(reset = ButtonWidget.builder(Text.literal("Reset"), (button) -> 
		{
			handler.resetTree();
			position = new Vec2f(-this.width / 4, -this.height / 4);
		}).dimensions(midPoint - 70 - 20, 7, 40, 16).build());
		addDrawableChild(save = ButtonWidget.builder(Text.literal("Save"), (button) -> 
		{
			SaveTreePacket.send(player, handler.tricksyUUID(), handler.getTree());
			client.setScreen(null);
		}).dimensions(midPoint + 70 - 20, 7, 40, 16).build());
		
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
					return true;
				case TYPE:
					if(hoveredNode.isRoot())	// Cannot replace the root node
						return false;
					
					int typeCount = TFNodeTypes.NODE_TYPES.size();
					int typeIndex = -1;
					for(int i=0; i<typeCount; i++)
						if(TFNodeTypes.NODE_TYPES.get(i) == hoveredNode.getType())
						{
							typeIndex = i;
							break;
						}
					
					if(typeIndex >= 0)
					{
						typeIndex += (int)amount;
						if(typeIndex < 0)
							typeIndex = typeCount - 1;
						else
							typeIndex %= typeCount;
						
						hoveredNode.parent().replaceChild(hoveredNode.getID(), TFNodeTypes.NODE_TYPES.get(typeIndex).create(hoveredNode.getID()));
						
						return true;
					}
					
					return true;
				case VARIABLES:
					// The input variable we are cycling
					WhiteboardRef inputRef = null;
					
					int index = Math.floorDiv((int)mouseY - hoveredNode.screenY - 24, 11);
					List<Pair<Text, Optional<Text>>> sortedVariables = NodeRenderUtils.getSortedVariables(hoveredNode);
					Text indexName = sortedVariables.get(index).getLeft();
					
					Map<WhiteboardRef, Predicate<WhiteboardRef>> variables = hoveredNode.getSubType().variableSet();
					for(WhiteboardRef ref : variables.keySet())
						if(ref.displayName().getString().equals(indexName.getString()))
						{
							inputRef = ref;
							break;
						}
					if(inputRef == null)
						return false;
					
					// The current whiteboard value in use (if any)
					WhiteboardRef valueRef = hoveredNode.variable(inputRef);
					
					List<WhiteboardRef> options = this.handler.getMatches(variables.get(inputRef));
					if(options.isEmpty())
						valueRef = null;
					else
					{
						options = WhiteboardRef.sortByDisplayName(options);
						int optionIndex = 0;
						if(valueRef != null)
							optionIndex = options.indexOf(valueRef);
						
						optionIndex += amount;
						if(optionIndex >= options.size())
							optionIndex -= options.size();
						
						if(optionIndex < 0)
							valueRef = null;
						else
							valueRef = options.get(optionIndex);
					}
					
					hoveredNode.assign(inputRef, valueRef);
					return true;
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
			addNode.active = hoveredNode.canAddChild();
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
		NodeRenderUtils.renderTree(root, context, this.textRenderer);
		
		NodeRenderUtils.drawTextures(context, (this.width - 200) / 2, 2, 0, 68, 200, 26, 255, 255, 255);
	}
	
	private static enum HoveredElement
	{
		TYPE,
		SUBTYPE,
		VARIABLES;
	}
}
