package com.lying.tricksy.screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.joml.Matrix4f;

import com.google.common.collect.Lists;
import com.lying.tricksy.TricksyFoxesClient;
import com.lying.tricksy.entity.ai.node.NodeSubType;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.reference.Reference;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

public class NodeRenderUtils
{
	public static final Identifier TREE_TEXTURES = new Identifier(Reference.ModInfo.MOD_ID, "textures/gui/behaviour_tree.png");
	public static final Identifier TREE_TEXTURES_OVERLAY = new Identifier(Reference.ModInfo.MOD_ID, "textures/gui/behaviour_tree_overlay.png");
	public static final Identifier LINE_TEXTURE = new Identifier(Reference.ModInfo.MOD_ID, "textures/gui/tree_branch.png");
	public static final Identifier LINE_TEXTURE_OVERLAY = new Identifier(Reference.ModInfo.MOD_ID, "textures/gui/tree_branch_overlay.png");
	
	public static final int NODE_SPACING = 10;
	public static final int CONNECTOR_OFFSET = 20;
	public static final int NODE_WIDTH = 150;
	
	public static void renderTree(TreeNode<?> node, DrawContext context, TextRenderer textRenderer)
	{
		NodeRenderUtils.drawNodeConnections(context, node, node.getType().color());
		NodeRenderUtils.renderNode(node, context, textRenderer);
	}
	
	public static void renderNode(TreeNode<?> node, DrawContext context, TextRenderer textRenderer)
	{
		drawNodeBackground(context, node, node.getType().color(), node.screenX, node.screenY);
		
		int drawY = node.screenY + 4;
		Text typeName = node.getType().translatedName();
		context.drawText(textRenderer, typeName, node.screenX + (NODE_WIDTH - textRenderer.getWidth(typeName)) / 2, drawY, -1, false);
		drawY += 11;
		
		NodeSubType<?> subType = node.getSubType();
		Text subName = subType.translatedName();
		context.drawText(textRenderer, subName, node.screenX + (NODE_WIDTH - textRenderer.getWidth(subName)) / 2, drawY, 0x404040, false);
		drawY += 11;
		
		for(Pair<Text, Optional<Text>> line : getSortedVariables(node))
		{
			context.drawText(textRenderer, line.getLeft(), node.screenX + 4 + (46 - textRenderer.getWidth(line.getLeft())) / 2, drawY, 0x404040, false);
			if(line.getRight().isPresent())
				context.drawText(textRenderer, line.getRight().get(), node.screenX + 50 + (100 - textRenderer.getWidth(line.getRight().get())) / 2, drawY, 0x404040, false);
			drawY += 11;
		}
		
		for(TreeNode<?> child : node.children())
			renderNode(child, context, textRenderer);
	}
	
	private static void drawNodeBackground(DrawContext context, TreeNode<?> node, int colour, int x, int y)
	{
        int r = ((colour & 0xFF0000) >> 16);
        int g = ((colour & 0xFF00) >> 8);
        int b = ((colour & 0xFF) >> 0);
		
		/* Type */
		int drawY = y;
		// Body
		drawTextures(context, x - 25, drawY - 6, 0, 0, 200, 19, r, g, b);
		drawY += 13;
		
		/* Subtype */
		int variables = node.getSubType().variableSet().size();
		drawTextures(context, x - 25, drawY, 0, variables == 0 ? 32 : 20, 200, variables == 0 ? 19 : 11, r, g, b);
		drawTextures(context, x + 25, drawY, 0, 145, 100, 11, r, g, b);
		
		/* Variables */
		if(variables > 0)
		{
			drawY += 11;
			for(int i=0; i<variables; i++)
			{
				boolean isLast = i == variables - 1;
				drawTextures(context, x - 25, drawY, 0, isLast ? 106 : 94, 200, isLast ? 19 : 11, r, g, b);
				drawY += 11;
			}
		}
		
		/* Child node connector */
		if(!node.children().isEmpty())
			drawTextures(context, node.screenX + CONNECTOR_OFFSET - 8, node.screenY + node.height - 8, 100, 145, 16, 16, r, g, b);
	}
	
	public static void drawNodeConnections(DrawContext context, TreeNode<?> node, int colour)
	{
		if(node.children().isEmpty())
			return;
        int r = ((colour & 0xFF0000) >> 16);
        int g = ((colour & 0xFF00) >> 8);
        int b = ((colour & 0xFF) >> 0);
		
		int startX = node.screenX + CONNECTOR_OFFSET;
		int startY = node.screenY + node.height;
		Vec2f branchStart = new Vec2f(startX, startY);
		
        TreeNode<?> lastChild = node.children().get(node.children().size() - 1);
		if(!TricksyFoxesClient.config.fancyTrees())
		{
			int col = decimalToARGB(colour);
			context.fill(startX - 1, startY, startX + 1, lastChild.screenY + lastChild.height / 2 + 1, col);
			
			node.children().forEach((child) -> 
			{
				int y = child.screenY + child.height / 2;
				context.fill(startX, y - 1, child.screenX, y + 1, col);
				drawNodeConnections(context, child, child.getType().color());
			});
			
			return;
		}
		
        // Main branch
        Vec2f branchEnd = new Vec2f(startX, lastChild.screenY + lastChild.height * 0.75F);
        int branchLength = (int)branchEnd.add(branchStart.negate()).length();
        List<Vec2f> branchPoints = Lists.newArrayList();
        branchPoints.add(branchStart);
        Vec2f line = branchEnd.add(branchStart.negate()).normalize();
        int segmentRate = 16;
        for(int i=0; i<Math.ceil(branchLength / segmentRate) + 1; i++)
        {
        	Vec2f point = branchStart.add(line.multiply(i * segmentRate));
        	double dist = point.add(branchStart.negate()).length();
        	double offsetAmount = (dist / branchLength) * 8;
        	point = point.add(new Vec2f((float)Math.sin(dist / 15D) * (float)offsetAmount, 0F));
        	
        	drawTexturedLine(context, branchPoints.get(i), point, 0, 0, 16, (int)point.add(branchPoints.get(i).negate()).length(), r, g, b);
        	branchPoints.add(point);
        }
        
		// Offshoots
		for(TreeNode<?> child : node.children())
		{
			Vec2f lineTarget = new Vec2f(child.screenX, child.screenY + (child.height / 2));
			
			/**
			 * Identify equivalent point on the main branch
			 * Pair the branch point with its direction at that point
			 * Determine angle from that direction to the target point
			 * Step from that point, with that direction, rotating until it enters the child node's area
			 */
			
			Vec2f start = branchStart;
			Vec2f dir = new Vec2f(0, 1F);
			float targetY = lineTarget.y - (child.getRNG().nextFloat() * child.height);
			if(targetY > branchStart.y)
			{
				// Find index of first branch point above the targetY
				for(int i=1; i<branchPoints.size(); i++)
				{
					Vec2f posA = branchPoints.get(i - 1);
					Vec2f posB = branchPoints.get(i);
					if(posB.y > targetY )
					{
						Vec2f direction = posB.add(posA.negate()).normalize();
						start = posA.add(direction.multiply((targetY - posA.y) * direction.y));
						dir = direction;
						break;
					}
				}
			}
			
			do
			{
				Vec2f direct = lineTarget.add(start.negate()).normalize();
				Vec2f offset = direct.add(dir.negate());
				dir = dir.add(offset.multiply(0.5F)).normalize();
				Vec2f nextPoint = start.add(dir.multiply(16F));
				drawTexturedLine(context, start, nextPoint, 0, 0, 16, 16, r, g, b);
				
				start = nextPoint;
			}
			while(!child.containsPoint((int)start.x, (int)start.y));
			
			// TODO Random chance of leaf/bush
			
			drawNodeConnections(context, child, child.getType().color());
		}
	}
	
	public static int nodeDisplayHeightRecursive(TreeNode<?> nodeIn)
	{
		int height = nodeDisplayHeight(nodeIn);
		List<TreeNode<?>> children = nodeIn.children();
		if(!children.isEmpty())
		{
			height += NODE_SPACING;
			for(int i=0; i<children.size(); i++)
			{
				height += nodeDisplayHeightRecursive(children.get(i));
				if(i != (children.size() - 1))
					height += NODE_SPACING;
			}
		}
		
		return height;
	}
	
	public static int nodeDisplayHeight(TreeNode<?> nodeIn)
	{
		int variables = nodeIn.getSubType().variableSet().size();
		
		// Type
		int height = 13;
		
		// Subtype
		height += (variables == 0 ? 15 : 11);
		
		// Variables
		if(variables > 0)
		{
			height += 11 * (variables - 1);
			height += 14;
		}
		
		return height + (TricksyFoxesClient.config.fancyTrees() ? nodeIn.getRNG().nextInt(4) : 0);
	}
	
	public static int decimalToARGB(int colour)
	{
        float r = ((colour & 0xFF0000) >> 16) / 255F;
        float g = ((colour & 0xFF00) >> 8) / 255F;
        float b = ((colour & 0xFF) >> 0) / 255F;
		return MathHelper.packRgb(r, g, b) | 255 << 24;
	}
	
	protected static void drawTextures(DrawContext context, int x, int y, int uv0, int uv1, int width, int height, int red, int green, int blue)
	{
		context.drawTexture(TREE_TEXTURES, x, y, uv0, uv1, width, height);
		drawTintedTexture(TREE_TEXTURES_OVERLAY, context, x, y, uv0, uv1, width, height, red, green, blue);
	}
	
	protected static void drawTexturedLine(DrawContext context, Vec2f start, Vec2f end, int uv0, int uv1, int width, int height, int red, int green, int blue)
	{
		drawTintedTexture(LINE_TEXTURE, context, start, end, uv0, uv1, width, height, 255, 255, 255);
		drawTintedTexture(LINE_TEXTURE_OVERLAY, context, start, end, uv0, uv1, width, height, red, green, blue);
	}
	
	protected static void drawTintedTexture(Identifier texture, DrawContext context, int x, int y, int uv0, int uv1, int width, int height, int red, int green, int blue)
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
	
	protected static void drawTintedTexture(Identifier texture, DrawContext context, Vec2f start, Vec2f end, int uv0, int uv1, int width, int height, int red, int green, int blue)
	{
		Vec2f dir = end.add(start.negate());
		dir = dir.normalize();
		dir = new Vec2f(dir.y, -dir.x);
		
		Vec2f a = start.add(dir.multiply(width * -0.5F));
		Vec2f d = start.add(dir.multiply(width * 0.5F));
		Vec2f b = end.add(dir.multiply(width * -0.5F));
		Vec2f c = end.add(dir.multiply(width * 0.5F));
		
		float u1 = (float)uv0 / 16F;
		float u2 = (float)(uv0 + width) / 16F;
		float v1 = (float)uv1 / 16F;
		float v2 = (float)(uv1 + height) / 16F;
		
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
        RenderSystem.enableBlend();
        Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
        bufferBuilder.vertex(matrix4f, a.x, a.y, 0).color(red, green, blue, 255).texture(u1, v1).next();
        bufferBuilder.vertex(matrix4f, b.x, b.y, 0).color(red, green, blue, 255).texture(u1, v2).next();
        bufferBuilder.vertex(matrix4f, c.x, c.y, 0).color(red, green, blue, 255).texture(u2, v2).next();
        bufferBuilder.vertex(matrix4f, d.x, d.y, 0).color(red, green, blue, 255).texture(u2, v1).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
	}
	
	public static List<Pair<Text, Optional<Text>>> getSortedVariables(TreeNode<?> node)
	{
		List<Pair<Text, Optional<Text>>> variablesToDisplay = Lists.newArrayList();
		for(WhiteboardRef input : node.getSubType().variableSet().keySet())
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
		return variablesToDisplay;
	}
}
