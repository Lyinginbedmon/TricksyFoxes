package com.lying.tricksy.screen;

import java.util.EnumSet;
import java.util.Optional;

import org.joml.Matrix4f;

import com.google.common.base.Predicates;
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.NodeStatusLog;
import com.lying.tricksy.entity.ai.NodeStatusLog.Log;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.screen.NodeRenderUtils.NodeRenderFlags;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
		NodeRenderUtils.drawTextures(context, (this.width - 200) / 2, 2, 0, 68, 200, 26, 255, 255, 255);
		context.drawText(textRenderer, this.title, (this.width - this.textRenderer.getWidth(this.title)) / 2, 8, 0x404040, false);
		if(this.theTricksy.isPresent())
		{
			Text name = this.theTricksy.get().getDisplayName();
			context.drawText(textRenderer, name, (this.width - this.textRenderer.getWidth(name)) / 2, 8 + this.textRenderer.fontHeight + 1, 0x404040, false);
		}
	}
	
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY)
	{
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
		
		NodeStatusLog latestLog = tricksy.getLatestLog();
		TreeNode<?> root = tricksy.getBehaviourTree().root(latestLog.tree());
		
		NodeRenderUtils.scaleAndPositionNode(root, renderX, renderY, Predicates.alwaysFalse(), false);
		NodeRenderUtils.renderTree(root, context, this.textRenderer, 0, Predicates.alwaysFalse(), false);
		renderBackground(context);
		
		NodeRenderUtils.renderTreeConditional(root, context, textRenderer, 0, node -> latestLog.wasActive(node), RENDER_FLAGS);
		
		latestLog.getActiveNodes().forEach(id -> 
		{
			TreeNode<?> node = root.getByID(id);
			if(node == null)
				return;
			
			Log latest = latestLog.getLog(id);
			if(latest.getLeft() == null)
				return;
			
			int iconX = node.screenX + node.width + 2;
			int iconY = node.screenY + (node.height - 16) / 2;
			Identifier texture = latest.getLeft().texture();
			int alpha = (int)(((float)latest.getRight() / (float)Log.DURATION) * 255F);
			renderTransparentIcon(texture, iconX, iconY, alpha, context);
			
			latest.message().ifPresent(text -> 
			{
				context.drawText(textRenderer, text, iconX + 18, iconY + (16 - textRenderer.fontHeight) / 2, -1, false);
			});
		});
	}
	
	private static void renderTransparentIcon(Identifier texture, int iconX, int iconY, int alpha, DrawContext context)
	{
		RenderSystem.setShaderTexture(0, texture);
		RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
		RenderSystem.enableBlend();
		Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
			bufferBuilder.vertex(matrix4f, iconX + 0, iconY + 0, 0).color(255, 255, 255, alpha).texture(0, 0).next();
			bufferBuilder.vertex(matrix4f, iconX + 0, iconY + 16, 0).color(255, 255, 255, alpha).texture(0, 1).next();
			bufferBuilder.vertex(matrix4f, iconX + 16, iconY + 16, 0).color(255, 255, 255, alpha).texture(1, 1).next();
			bufferBuilder.vertex(matrix4f, iconX + 16, iconY + 0, 0).color(255, 255, 255, alpha).texture(1, 0).next();
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		RenderSystem.disableBlend();
	}
}
