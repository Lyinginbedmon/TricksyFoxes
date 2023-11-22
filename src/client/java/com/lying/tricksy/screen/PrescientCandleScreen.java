package com.lying.tricksy.screen;

import java.util.EnumSet;
import java.util.Optional;

import com.google.common.base.Predicates;
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.NodeStatusLog;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.screen.NodeRenderUtils.NodeRenderFlags;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;

public class PrescientCandleScreen extends HandledScreen<PrescientCandleScreenHandler>
{
	public static final EnumSet<NodeRenderFlags> RENDER_FLAGS = EnumSet.of(NodeRenderFlags.TYPE, NodeRenderFlags.SUBTYPE, NodeRenderFlags.CHILDREN);
	
	private Vec2f position = Vec2f.ZERO;
	private Vec2f moveStart = null;
	
	private Optional<LivingEntity> theTricksy = Optional.empty();
	
	public PrescientCandleScreen(PrescientCandleScreenHandler handler, PlayerInventory inventory, Text title)
	{
		super(handler, inventory, title);
	}
	
	protected void init()
	{
		position = new Vec2f(-this.width / 4, -this.height / 4);
	}
	
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
	
	protected void drawForeground(DrawContext context, int mouseX, int mouseY)
	{
		context.drawText(textRenderer, this.title, (this.width - this.textRenderer.getWidth(this.title)) / 2, 2 + (26 - this.textRenderer.fontHeight) / 2, 0x404040, false);
		
		if(this.theTricksy.isPresent())
			context.drawText(textRenderer, this.theTricksy.get().getDisplayName(), (this.width - this.textRenderer.getWidth(this.title)) / 2, 2 + (26 + this.textRenderer.fontHeight + 1) / 2, 0x404040, false);
	}
	
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY)
	{
		renderBackground(context);
		this.theTricksy = getScreenHandler().getTricksyMob(client.world, client.player);
		if(this.theTricksy.isEmpty())
			return;
		
		ITricksyMob<?> tricksy = (ITricksyMob<?>)this.theTricksy.get();
		
		int renderX = this.width / 2 + (int)position.x;
		int renderY = this.height / 2 + (int)position.y;
		if(isDragging())
		{
			int offsetX = mouseX - (int)moveStart.x;
			int offsetY = mouseY - (int)moveStart.y;
			
			renderX += offsetX;
			renderY += offsetY;
		}
		
		TreeNode<?> root = tricksy.getBehaviourTree().root();
		NodeRenderUtils.scaleAndPositionNode(root, renderX, renderY, Predicates.alwaysFalse(), false);
		NodeRenderUtils.renderTree(root, context, this.textRenderer, 0, Predicates.alwaysFalse(), false);
		renderBackground(context);
		
		NodeStatusLog latestLog = tricksy.getLatestLog();
		latestLog.getActiveNodes().forEach(id -> 
		{
			TreeNode<?> node = root.getByID(id);
			if(node == null)
				return;
			
			NodeRenderUtils.renderNode(node, context, textRenderer, 0, RENDER_FLAGS);
		});
		
		NodeRenderUtils.drawTextures(context, (this.width - 200) / 2, 2, 0, 68, 200, 26, 255, 255, 255);
	}
}
