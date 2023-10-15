package com.lying.tricksy.screen;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import com.google.common.base.Predicates;
import com.lying.tricksy.TricksyFoxesClient;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.network.SaveTreePacket;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.screen.NodeRenderUtils.NodeDisplay;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec2f;

@Environment(EnvType.CLIENT)
public class TreeScreen extends TricksyScreenBase
{
	private TreeNode<?> hoveredNode = null;
	private HoveredElement hoveredPart = null;
	
	// Button to add a new node
	public ButtonWidget addNode;
	// Button to delete a node
	public ButtonWidget delNode;
	// Button reset to the tree the fox is using
	public ButtonWidget reset;
	// Button to set the fox's tree to this one
	public ButtonWidget save;
	// Button to view whiteboards
	public ButtonWidget whiteboards;
	
	private Vec2f position = null;
	private Vec2f moveStart = null;
	private NodeDisplay showVariables = TricksyFoxesClient.config.nodeDisplayStyle();
	
	public TreeScreen(TricksyTreeScreenHandler handler, PlayerInventory playerInventory, Text title)
	{
		super(handler, playerInventory, title);
	}
	
	protected void init()
	{
		addDrawableChild(addNode = makeTexturedWidget(16, 16, 0, 184, (button) -> 
		{
			this.hoveredNode.addChild(TFNodeTypes.LEAF.create(UUID.randomUUID()), hasShiftDown());
			this.handler.countNodes();
		}));
		addDrawableChild(delNode = makeTexturedWidget(16, 16, 16, 184, (button) -> 
		{
			this.handler.getTree().root().removeChild(hoveredNode);
			this.handler.countNodes();
		}));
		
		int midPoint = this.width / 2;
		addDrawableChild(reset = ButtonWidget.builder(Text.translatable("gui."+Reference.ModInfo.MOD_ID+".tree_screen.reset"), (button) -> 
		{
			handler.resetTree();
			setPosition(-this.width / 4, -this.height / 4);
		}).dimensions(midPoint - 70 - 20, 7, 40, 16).build());
		addDrawableChild(save = ButtonWidget.builder(Text.translatable("gui."+Reference.ModInfo.MOD_ID+".tree_screen.save"), (button) -> 
		{
			SaveTreePacket.send(player, handler.tricksyUUID(), handler.getTree());
			client.currentScreen.close();
		}).dimensions(midPoint + 70 - 20, 7, 40, 16).build());
		addDrawableChild(whiteboards = makeTexturedWidget((this.width / 2) + 34, 18, 32, 184, (button) -> 
		{
			client.setScreen(new WhiteboardScreen(getScreenHandler(), this.playerInv, this.title));
		}));
		
		if(position == null)
			setPosition(-this.width / 4, -this.height / 4);
	}
	
	public void setPosition(int x, int y)
	{
		this.position = new Vec2f(x, y);
	}
	
	public Vec2f position()
	{
		return this.position == null ? Vec2f.ZERO : this.position; 
	}
	
	public boolean mouseClicked(double x, double y, int mouseKey)
	{
		if(mouseKey == 0)
		{
			if(hoveredNode == null)
			{
				for(Element widget : children())
				{
					if(!widget.isMouseOver(x, y) || !(widget instanceof ClickableWidget))
						continue;
					return widget.mouseClicked(x, y, mouseKey);
				}
				
				this.setDragging(true);
				this.moveStart = new Vec2f((float)x, (float)y);
			}
			else
				client.setScreen(new NodeScreen(getScreenHandler(), this.playerInv, this.title, hoveredNode));
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
					int index = Math.floorDiv((int)mouseY - hoveredNode.screenY - 24, 11);
					List<Pair<WhiteboardRef, Optional<WhiteboardRef>>> sortedVariables = NodeRenderUtils.getSortedVariables(hoveredNode);
					if(index >= sortedVariables.size())
						return false;
					WhiteboardRef input = sortedVariables.get(index).getLeft();
					hoveredNode.assign(input, incrementOption(hoveredNode, input, (int)Math.signum(amount)));
					return true;
			}
		}
		return super.mouseScrolled(mouseX, mouseY, amount);
	}
	
	private WhiteboardRef incrementOption(TreeNode<?> node, WhiteboardRef inputRef, int scroll)
	{
		WhiteboardRef valueRef = node.variable(inputRef);
		if(scroll == 0)
			return valueRef;
		
		List<WhiteboardRef> options = this.handler.getMatches(node.getSubType().variableSet().get(inputRef).predicate(), null);
		if(options.isEmpty())
			return null;
		else if(valueRef == null)
			return scroll > 0 ? options.get(0) : null;
		else
		{
			options.sort(WhiteboardRef.REF_SORT);
			
			int optionIndex = 0;
			for(int i=0; i<options.size(); i++)
				if(options.get(i).isSameRef(valueRef))
				{
					optionIndex = i;
					break;
				}
			
			optionIndex += Math.signum(scroll);
			return optionIndex < 0 ? null : options.get(optionIndex % options.size());
		}
	}
	
	public boolean mouseReleased(double x, double y, int mouseKey)
	{
		if(mouseKey == 0 && isDragging())
		{
			float xOff = (float)x - moveStart.x;
			float yOff = (float)y - moveStart.y;
			
			position = position().add(new Vec2f(xOff, yOff));
			
			this.setDragging(false);
			this.moveStart = null;
			
			return true;
		}
		return super.mouseReleased(mouseKey, mouseKey, mouseKey);
	}
	
	protected void drawForeground(DrawContext context, int mouseX, int mouseY)
	{
		NodeRenderUtils.drawTextures(context, (this.width - 200) / 2, 2, 0, 68, 200, 26, 255, 255, 255);
		context.drawText(textRenderer, this.title, (this.width - this.textRenderer.getWidth(this.title)) / 2, 2 + (26 - this.textRenderer.fontHeight) / 2, 0x404040, false);
		
		this.reset.render(context, mouseX, mouseY, 0F);
		this.save.render(context, mouseX, mouseY, 0F);
		this.whiteboards.render(context, mouseX, mouseY, 0F);
		
		TreeNode<?> root = handler.getTree().root();
		if(mouseY < 28 && Math.abs((this.width / 2) - mouseX) < 100)
			hoveredNode = null;
		else
			hoveredNode = isDragging() ? null : root.findNodeAt(mouseX, mouseY);
		
		if(hoveredNode != null)
		{
			boolean additionAllowed = this.player.isCreative() || this.handler.canAddNode();
			addNode.visible = additionAllowed; 
			delNode.visible = true;
			addNode.active = hoveredNode.canAddChild() && additionAllowed;
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
		if(hoveredNode == null)
			this.addNode.visible = this.delNode.visible = false;
		renderBackground(context);
		if(handler.getTree() == null)
			return;
		
		TreeNode<?> root = handler.getTree().root();
		int renderX = this.width / 2 + (int)position().x;
		int renderY = this.height / 2 + (int)position().y;
		if(isDragging())
		{
			int offsetX = mouseX - (int)moveStart.x;
			int offsetY = mouseY - (int)moveStart.y;
			
			renderX += offsetX;
			renderY += offsetY;
		}
		
		Predicate<TreeNode<?>> variableShow = null;
		switch(showVariables)
		{
			case ALWAYS:
				variableShow = Predicates.alwaysTrue();
				break;
			case HOVERED:
				if(this.hoveredNode != null)
					variableShow = (node) -> node.getID().equals(this.hoveredNode.getID());
				else
					variableShow = Predicates.alwaysFalse();
				break;
			default:
			case NEVER:
				variableShow = Predicates.alwaysFalse();
				break;
		}
		
		NodeRenderUtils.scaleAndPositionNode(root, renderX, renderY, variableShow, true);
		NodeRenderUtils.renderTree(root, context, this.textRenderer, this.ticksOpen, variableShow, true);
	}
	
	public static enum HoveredElement
	{
		TYPE,
		SUBTYPE,
		VARIABLES;
	}
}
