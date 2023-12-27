package com.lying.tricksy.screen;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import com.google.common.base.Predicates;
import com.lying.tricksy.TricksyFoxesClient;
import com.lying.tricksy.entity.ai.BehaviourTree;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.whiteboard.CommandWhiteboard.Order;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.network.AddLocalReferencePacket;
import com.lying.tricksy.network.DeleteReferencePacket;
import com.lying.tricksy.network.SaveTreePacket;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.screen.NodeRenderUtils.NodeDisplay;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;

@Environment(EnvType.CLIENT)
public class TreeScreen extends TricksyScreenBase
{
	private TreeNode<?> hoveredNode = null;
	
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
			this.root().removeChild(hoveredNode);
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
			issueWhiteboardPackets(player, handler);
			SaveTreePacket.send(player, handler.tricksyUUID(), getTree());
			client.currentScreen.close();
		}).dimensions(midPoint + 70 - 20, 7, 40, 16).build());
		addDrawableChild(whiteboards = makeTexturedWidget((this.width / 2) + 34, 18, 32, 184, (button) -> 
		{
			client.setScreen(new WhiteboardScreen(getScreenHandler(), this.playerInv, this.title));
		}));
		
		// TODO Display available trees more aesthetically
		addDrawableChild(ButtonWidget.builder(Text.translatable("order."+Reference.ModInfo.MOD_ID+".idle"), (button) -> this.handler.showSubTree(null)).dimensions(0, 0, 35, 20).build());
		for(Order order : Order.values())
			addDrawableChild(ButtonWidget.builder(order.translate(), (button) -> this.handler.showSubTree(order)).dimensions(0, (1+order.ordinal()) * 22, 35, 20).build());
		
		if(position == null)
			setPosition(-this.width / 4, -this.height / 4);
	}
	
	public void handledScreenTick()
	{
		super.handledScreenTick();
		save.active = handler.canSyncToServer();
	}
	
	public void setPosition(int x, int y)
	{
		this.position = new Vec2f(x, y);
	}
	
	public Vec2f position()
	{
		return this.position == null ? Vec2f.ZERO : this.position; 
	}
	
	public BehaviourTree getTree() { return this.handler.getTree(); }
	
	public TreeNode<?> root() { return this.handler.root(); }
	
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
			else if(!super.childrenMouseClicked(x, y, mouseKey))
				client.setScreen(new NodeScreen(getScreenHandler(), this.playerInv, this.title, hoveredNode));
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
		
		TreeNode<?> root = root();
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
		}
		else
		{
			addNode.visible = addNode.active = false;
			delNode.visible = delNode.active = false;
		}
	}
	
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY)
	{
		if(hoveredNode == null)
			this.addNode.visible = this.delNode.visible = false;
		renderBackground(context);
		if(getTree() == null)
			return;
		
		TreeNode<?> root = root();
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
	
	private void issueWhiteboardPackets(PlayerEntity player, TricksyTreeScreenHandler handler)
	{
		List<WhiteboardRef> addedReferences = handler.getAdditions();
		List<WhiteboardRef> deletedReferences = handler.getDeletions();
		
		/** Remove all new references marked for deletion, removing them from the deletion list as well */
		addedReferences.removeIf(ref -> 
		{
			if(handler.isMarkedForDeletion(ref))
			{
				deletedReferences.removeIf(del -> del.isSameRef(ref));
				return true;
			}
			return false;
		});
		
		UUID tricksyID = handler.tricksyUUID();
		deletedReferences.forEach(ref -> DeleteReferencePacket.send(player, tricksyID, ref));
		addedReferences.forEach(ref -> AddLocalReferencePacket.send(player, tricksyID, ref));
	}
	
	public static enum NodeElement
	{
		TYPE,
		SUBTYPE,
		VARIABLES;
	}
}
