package com.lying.tricksy.screen;

import java.util.List;
import java.util.Random;

import org.joml.Matrix4f;

import com.google.common.collect.Lists;
import com.lying.tricksy.reference.Reference;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;

public class BranchLine
{
	public static final Identifier BUSH_TEXTURE = new Identifier(Reference.ModInfo.MOD_ID, "textures/gui/tree_foliage.png");
	
	private final List<Quad> quadList = Lists.newArrayList();
	private final List<Vec2f> bushList = Lists.newArrayList();
	
	public BranchLine(List<Vec2f> points, Random rand, Identifier flowerTexture)
	{
		// Generate quads
		boolean mirror = false;
		for(int i=1; i<points.size(); i++)
		{
			Vec2f start = points.get(i-1);
			Vec2f end = points.get(i);
			quadList.add(Quad.fromLine(start, end, mirror = !mirror, rand.nextInt(4) == 0 ? flowerTexture : null));
		}
		
		// Merge quads together to prevent line breaks
		for(int i=1; i<quadList.size(); i++)
		{
			Quad prev = quadList.get(i-1);
			Quad quad = quadList.get(i);
			
			Vec2f ab = prev.b.add(quad.a).multiply(0.5F);
			Vec2f dc = prev.c.add(quad.d).multiply(0.5F);
			
			prev.b = ab;
			quad.a = ab;
			
			prev.c = dc;
			quad.d = dc;
		}
		
		// Generate bushes
		int bushes = rand.nextInt(Math.floorDiv(points.size(), 3));
		if(bushes > 0)
			while(bushes-- > 0 && points.size() > 3)
				bushList.add(points.remove(rand.nextInt(1, points.size() - 2)));
	}
	
	public void render(DrawContext context)
	{
		// Render quads
		quadList.forEach((quad) -> quad.render(context));
	}
	
	public void renderBushes(DrawContext context)
	{
		bushList.forEach((point) -> context.drawTexture(BranchLine.BUSH_TEXTURE, (int)point.x - 8, (int)point.y - 8, 0, 0, 16, 16, 16, 16));
	}
	
	private static class Quad
	{
		public static final Identifier LINE_TEXTURE = new Identifier(Reference.ModInfo.MOD_ID, "textures/gui/tree_branch.png");
		
		/**
		 * a	d
		 * 
		 * b	c
		 */
		
		public Vec2f a, b, c, d;
		public final boolean mirrored;
		public final boolean flowering;
		
		private final Identifier flowerTexture;
		
		private Quad(Vec2f aIn, Vec2f bIn, Vec2f cIn, Vec2f dIn, boolean mirrored, Identifier flowers)
		{
			this.a = aIn;
			this.b = bIn;
			this.c = cIn;
			this.d = dIn;
			this.mirrored = mirrored;
			
			this.flowering = flowers != null;
			this.flowerTexture = flowers;
		}
		
		public static Quad fromLine(Vec2f start, Vec2f end, boolean mirror, Identifier flowerTexture)
		{
			Vec2f dir = end.add(start.negate());
			dir = dir.normalize();
			dir = new Vec2f(dir.y, -dir.x);
			
			Vec2f vecA = start.add(dir.multiply(16 * -0.5F));
			Vec2f vecB = end.add(dir.multiply(16 * -0.5F));
			Vec2f vecC = end.add(dir.multiply(16 * 0.5F));
			Vec2f vecD = start.add(dir.multiply(16 * 0.5F));
			return new Quad(vecA, vecB, vecC, vecD, mirror, flowerTexture);
		}
		
		public void render(DrawContext context)
		{
			drawTexture(LINE_TEXTURE, context, a, b, c, d, mirrored);
			if(flowering)
				drawTexture(flowerTexture, context, a, b, c, d, mirrored, 255, 255, 255);
		}
		
		private static void drawTexture(Identifier texture, DrawContext context, Vec2f a, Vec2f b, Vec2f c, Vec2f d, boolean mirror)
		{
			drawTexture(texture, context, a, b, c, d, mirror, 255, 255, 255);
		}
		
		private static void drawTexture(Identifier texture, DrawContext context, Vec2f a, Vec2f b, Vec2f c, Vec2f d, boolean mirror, int red, int green, int blue)
		{
			float u1 = (float)(mirror ? 16 : 0) / 16F;
			float u2 = (float)(mirror ? 0 : 16) / 16F;
			
	        RenderSystem.setShaderTexture(0, texture);
	        RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
	        RenderSystem.enableBlend();
	        Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
	        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
	        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
	        bufferBuilder.vertex(matrix4f, a.x, a.y, 0).color(red, green, blue, 255).texture(u1, 0F).next();
	        bufferBuilder.vertex(matrix4f, b.x, b.y, 0).color(red, green, blue, 255).texture(u1, 1F).next();
	        bufferBuilder.vertex(matrix4f, c.x, c.y, 0).color(red, green, blue, 255).texture(u2, 1F).next();
	        bufferBuilder.vertex(matrix4f, d.x, d.y, 0).color(red, green, blue, 255).texture(u2, 0F).next();
	        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
	        RenderSystem.disableBlend();
		}
	}
}
