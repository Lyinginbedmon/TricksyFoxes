package com.lying.tricksy.renderer;

import java.util.List;

import org.joml.Matrix4f;

import com.lying.tricksy.entity.ai.whiteboard.OrderWhiteboard.Order;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.utility.TricksyOrders;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;

public class OrderOverlay
{
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	private static final Identifier BORDER_TEX = new Identifier(Reference.ModInfo.MOD_ID, "textures/gui/orders/overlay.png");
	private static final int ICON_SIZE = 16;
	
	public static void drawHud(DrawContext context, float partialTicks)
	{
		if(!TricksyOrders.shouldRenderOrders())
			return;
		
		final int width = mc.getWindow().getScaledWidth();
		final int height = mc.getWindow().getScaledHeight();
		
		Order order = TricksyOrders.currentOrder();
		if(order == null)
			return;
		
		MutableText orderTitle = order.translate(TricksyOrders.currentTarget());
		context.drawText(mc.textRenderer, orderTitle, (width - mc.textRenderer.getWidth(orderTitle)) / 2, height / 2 - 20, -1, false);
		
		List<Order> options = TricksyOrders.options();
		if(options.size() > 1)
		{
			int leftRight = Math.min(3, (options.size() - 1) / 2);
			leftRight = 3;
			
			int offset = (ICON_SIZE / 2) + 1;
			for(int i=1; i<leftRight; i++)
			{
				int size = ICON_SIZE - (4 * i);
				offset += (size / 2);
				Order option = TricksyOrders.getNextLast(-i);
				drawCentredIcon(context, option, (width / 2) - offset, (height / 2) + 20, size, size, 0.75F, 1F);
				
				option = TricksyOrders.getNextLast(i);
				drawCentredIcon(context, option, (width / 2) + offset, (height / 2) + 20, size, size, 0.75F, 1F);
				
				offset += (size / 2) + 1;
			}
		}
		
		drawCentredIcon(context, order, width / 2, height / 2 + 20, 16, 16, 1F, 1F);
	}
	
	private static void drawCentredIcon(DrawContext context, Order order, int x, int y, int width, int height, float r, float alpha)
	{
		drawIcon(context, order, x - width / 2, y - height / 2, width, height, r, alpha);
	}
	
	private static void drawIcon(DrawContext context, Order order, int x, int y, int width, int height, float light, float alpha)
	{
		drawIcon(context, order, x, y, width, height, light, light, light, alpha);
	}
	
	private static void drawIcon(DrawContext context, Order order, int x, int y, int width, int height, float r, float g, float b, float alpha)
	{
		int color = order.color();
        float borderR = (float)((color & 0xFF0000) >> 16) / 255F;
        float borderG = (float)((color & 0xFF00) >> 8) / 255F;
        float borderB = (float)((color & 0xFF) >> 0) / 255F;
		drawIcon(context, BORDER_TEX, x, y, width, height, borderR * r, borderG * g, borderB * b, alpha);
		
		drawIcon(context, order.texture(), x, y, width, height, r, g, b, alpha);
	}
	
	private static void drawIcon(DrawContext context, Identifier texture, int x, int y, int width, int height, float r, float g, float b, float alpha)
	{
		RenderSystem.setShaderTexture(0, texture);
		RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
		RenderSystem.enableBlend();
		Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
			buffer.vertex(matrix4f, x + 0,		y + 0, 0).color(r, g, b, alpha).texture(0F, 0F).next();
			buffer.vertex(matrix4f, x + 0,		y + height, 0).color(r, g, b, alpha).texture(0F, 1F).next();
			buffer.vertex(matrix4f, x + width,	y + height, 0).color(r, g, b, alpha).texture(1F, 1F).next();
			buffer.vertex(matrix4f, x + width,	y + 0, 0).color(r, g, b, alpha).texture(1F, 0F).next();
		BufferRenderer.drawWithGlobalProgram(buffer.end());
		RenderSystem.disableBlend();
	}
}
