package com.lying.tricksy.screen;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import com.google.common.collect.Lists;
import com.lying.tricksy.TricksyFoxesClient;
import com.lying.tricksy.entity.ai.node.INodeValue;
import com.lying.tricksy.entity.ai.node.INodeValue.WhiteboardValue;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.node.handler.INodeInput;
import com.lying.tricksy.entity.ai.node.subtype.NodeSubType;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFObjType;
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
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

public class NodeRenderUtils
{
	public static final Identifier TREE_TEXTURES = new Identifier(Reference.ModInfo.MOD_ID, "textures/gui/behaviour_tree.png");
	public static final Identifier TREE_TEXTURES_OVERLAY = new Identifier(Reference.ModInfo.MOD_ID, "textures/gui/behaviour_tree_overlay.png");
	
	public static final int NODE_SPACING = 10;
	public static final int CONNECTOR_OFFSET = 20;
	public static final int NODE_WIDTH = 150;
	
	public static void renderTree(TreeNode<?> node, DrawContext context, TextRenderer textRenderer, int ticksOpen, Predicate<TreeNode<?>> showVariables, boolean allowDiscretion)
	{
		EnumSet<NodeRenderFlags> flags = EnumSet.allOf(NodeRenderFlags.class);
		if(allowDiscretion)
			flags.remove(NodeRenderFlags.CHILDREN);
		NodeRenderUtils.drawNodeConnections(context, node, node.getType().color(), allowDiscretion);
		NodeRenderUtils.renderNodeRecursive(node, context, textRenderer, ticksOpen, showVariables, flags);
	}
	
	public static void renderNodeRecursive(TreeNode<?> node, DrawContext context, TextRenderer textRenderer, int ticksOpen, Predicate<TreeNode<?>> showVariables, EnumSet<NodeRenderFlags> flags)
	{
		if(showVariables.test(node))
			flags.add(NodeRenderFlags.VARIABLES);
		else
			flags.remove(NodeRenderFlags.VARIABLES);
		renderNode(node, context, textRenderer, ticksOpen, flags);
		if(!node.isDiscrete(!flags.contains(NodeRenderFlags.CHILDREN)))
			node.children().forEach((child) -> renderNodeRecursive(child, context, textRenderer, ticksOpen, showVariables, flags));
	}
	
	public static void renderNode(TreeNode<?> node, DrawContext context, TextRenderer textRenderer, int ticksOpen, EnumSet<NodeRenderFlags> flags)
	{
		drawNodeBackground(context, node, node.getType().color(), node.screenX, node.screenY, flags.contains(NodeRenderFlags.VARIABLES));
		
		int drawY = node.screenY + 4;
		if(flags.contains(NodeRenderFlags.TYPE))
		{
			Text typeName = node.getDisplayName();
			context.drawText(textRenderer, typeName, node.screenX + (NODE_WIDTH - textRenderer.getWidth(typeName)) / 2, drawY, -1, false);
		}
		drawY += 11;
		
		NodeSubType<?> subType = node.getSubType();
		if(flags.contains(NodeRenderFlags.SUBTYPE))
		{
			Text subName = subType.translatedName();
			if(textRenderer.getWidth(subName) > 80)
			{
				// Original width
				int width = textRenderer.getWidth(subName);
				
				// Original number of characters
				int length = subName.getString().length();
				
				int trimAmount = (int)((1F - 75F / (float)width) * length);
				if(trimAmount%2 > 0)
					trimAmount++;
				
				int offset = trimAmount / 2;
				
				int start = offset + (int)(Math.sin((double)ticksOpen * 0.15D) * offset);
				int end = start + (length - trimAmount);
				subName = Text.literal(subName.getString().substring(start - 1, end + 1));
			}
			context.drawText(textRenderer, subName, node.screenX + (NODE_WIDTH - textRenderer.getWidth(subName)) / 2, drawY + 1, 0x404040, false);
		}
		drawY += 11;
		if(flags.contains(NodeRenderFlags.VARIABLES))
		{
			for(Pair<WhiteboardRef, Optional<INodeValue>> line : getSortedVariables(node))
			{
				INodeInput input = subType.getInput(line.getLeft());
				if(input == null)
					continue;
				
				renderReference(line.getLeft(), context, textRenderer, node.screenX + 4, drawY, 45, true, input.isOptional());
				if(line.getRight().isPresent())
				{
					switch(line.getRight().get().type())
					{
						case WHITEBOARD:
							renderReference(((WhiteboardValue)line.getRight().get()).assignment(), context, textRenderer, node.screenX + 52, drawY, 94, false, false);
							break;
						case STATIC:
							renderStatic(line.getRight().get(), context, textRenderer, node.screenX + 52, drawY);
							break;
					}
				}
				else
				{
					Text defaultName = input.describeValue();
					context.drawText(textRenderer, defaultName, node.screenX + 52 + (94 - textRenderer.getWidth(defaultName)) / 2, drawY, 0x808080, false);
				}
				drawY += 11;
			}
		}
	}
	
	public static void renderStatic(INodeValue reference, DrawContext context, TextRenderer textRenderer, int x, int y)
	{
		Text defaultName = reference.displayName();
		context.drawText(textRenderer, defaultName, x + (94 - textRenderer.getWidth(defaultName)) / 2, y, 0x808080, false);
	}
	
	public static void renderReference(WhiteboardRef reference, DrawContext context, TextRenderer textRenderer, int x, int y, int maxWidth, boolean iconRight, boolean isOptional)
	{
		int iconX = x + (iconRight ? maxWidth - 8 : 0);
		
		maxWidth -= 8;
		if(maxWidth > 0)
		{
			MutableText name = reference.displayName().copy();
			boolean centred = true;
			if(textRenderer.getWidth(name) > maxWidth)
			{
				centred = false;
				String display = "";
				while(textRenderer.getWidth(display) < (maxWidth - 4))
					display += name.getString().charAt(display.length());
				name = Text.literal(display + "...");
			}
			if(isOptional)
				name.formatted(Formatting.ITALIC);
			context.drawText(textRenderer, name, x + (iconRight ? 0 : 8) + (centred ? (maxWidth - textRenderer.getWidth(name)) / 2 : 0), y, 0x404040, false);
		}
		
		renderRefType(reference.type(), context, iconX, y, 8, 8);
	}
	
	public static void renderRefType(TFObjType<?> type, DrawContext context, int x, int y, int width, int height)
	{
		int xMin = x;
		int xMax = xMin + width - 1;
		int yMin = y;
		int yMax = yMin + height - 1;
		RenderSystem.setShaderTexture(0, type.texture());
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
		BufferBuilder builder = Tessellator.getInstance().getBuffer();
		builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
			builder.vertex(matrix4f, xMin, yMin, 0).texture(0F, 0F).next();
			builder.vertex(matrix4f, xMin, yMax, 0).texture(0F, 1F).next();
			builder.vertex(matrix4f, xMax, yMax, 0).texture(1F, 1F).next();
			builder.vertex(matrix4f, xMax, yMin, 0).texture(1F, 0F).next();
		BufferRenderer.drawWithGlobalProgram(builder.end());
	}
	
	private static void drawNodeBackground(DrawContext context, TreeNode<?> node, int colour, int x, int y, boolean showVariables)
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
		int variables = showVariables ? node.getSubType().variableSet().size() : 0;
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
		if(node.hasChildren())
			drawTextures(context, node.screenX + CONNECTOR_OFFSET - 8, node.screenY + node.height - 8, 100, 145, 16, 16, r, g, b);
	}
	
	public static void drawNodeConnections(DrawContext context, TreeNode<?> node, int colour, boolean allowDiscretion)
	{
		if(!node.hasChildren() || node.isDiscrete(allowDiscretion))
			return;
		
		Random rand = node.getRNG();
        Identifier flowerTex = node.getType().flowerTexture();
		
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
				drawNodeConnections(context, child, child.getType().color(), allowDiscretion);
			});
			
			return;
		}
		
        // Main branch
        Vec2f branchEnd = new Vec2f(startX, lastChild.screenY + lastChild.height * 0.75F);
        int branchLength = (int)branchEnd.add(branchStart.negate()).length();
        List<Vec2f> mainPoints = Lists.newArrayList();
        mainPoints.add(branchStart);
        Vec2f line = branchEnd.add(branchStart.negate()).normalize();
        int segmentRate = 16;
        for(int i=0; i<Math.ceil(branchLength / segmentRate) + 1; i++)
        {
        	Vec2f point = branchStart.add(line.multiply(i * segmentRate));
        	double dist = point.add(branchStart.negate()).length();
        	double offsetAmount = (dist / branchLength) * 8;
        	mainPoints.add(point.add(new Vec2f((float)Math.sin(dist / 15D) * (float)offsetAmount, 0F)));
        }
        BranchLine mainLine = new BranchLine(mainPoints, rand, flowerTex);
        mainLine.render(context);
        
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
			
			List<Vec2f> offshootPoints = Lists.newArrayList();
			
			Vec2f start = branchStart;
			Vec2f dir = new Vec2f(0, 1F);
			float targetY = lineTarget.y - (child.getRNG().nextFloat() * child.height);
			if(targetY > branchStart.y)
			{
				// Find index of first branch point above the targetY
				for(int i=1; i<mainPoints.size(); i++)
				{
					Vec2f posA = mainPoints.get(i - 1);
					Vec2f posB = mainPoints.get(i);
					if(posB.y > targetY )
					{
						Vec2f direction = posB.add(posA.negate()).normalize();
						start = posA.add(direction.multiply((targetY - posA.y) * direction.y));
						dir = direction;
						break;
					}
				}
			}
			offshootPoints.add(start);
			
			do
			{
				Vec2f direct = lineTarget.add(start.negate()).normalize();
				Vec2f offset = direct.add(dir.negate());
				dir = dir.add(offset.multiply(0.5F)).normalize();
				Vec2f nextPoint = start.add(dir.multiply(16F));
				
				start = nextPoint;
				offshootPoints.add(start);
			}
			while(!child.containsPoint((int)start.x, (int)start.y));
			
	        (new BranchLine(offshootPoints, rand, flowerTex)).render(context);
			
			drawNodeConnections(context, child, child.getType().color(), allowDiscretion);
		}
		
		mainLine.renderBushes(context);
	}
	
	/** Recursively positions and scales all nodes */
	public static void scaleAndPositionNode(TreeNode<?> node, int x, int y, Predicate<TreeNode<?>> includeVariables, boolean allowDiscretion)
	{
		node.setPositionAndWidth(x, y, 150, NodeRenderUtils.nodeDisplayHeight(node, includeVariables.test(node)));
		int childY = node.screenY + node.height + NodeRenderUtils.NODE_SPACING;
		for(TreeNode<?> child : node.children())
		{
			if(node.isDiscrete(allowDiscretion))
			{
				scaleAndPositionNode(child, Integer.MAX_VALUE, Integer.MAX_VALUE, includeVariables, allowDiscretion);
				continue;
			}
			Random childRNG = child.getRNG();
			int xOffset = TricksyFoxesClient.config.fancyTrees() ? childRNG.nextInt(2, 8) * 5 : 10;
			scaleAndPositionNode(child, node.screenX + NodeRenderUtils.CONNECTOR_OFFSET + xOffset, childY, includeVariables, allowDiscretion);
			childY += NodeRenderUtils.nodeDisplayHeightRecursive(child, includeVariables, allowDiscretion) + NodeRenderUtils.NODE_SPACING;
		}
	}
	
	public static int nodeDisplayHeightRecursive(TreeNode<?> nodeIn, Predicate<TreeNode<?>> includeVariables, boolean allowDiscretion)
	{
		int height = nodeDisplayHeight(nodeIn, includeVariables.test(nodeIn));
		if(nodeIn.hasChildren() && !nodeIn.isDiscrete(allowDiscretion))
		{
			List<TreeNode<?>> children = nodeIn.children();
			height += NODE_SPACING;
			for(int i=0; i<children.size(); i++)
			{
				height += nodeDisplayHeightRecursive(children.get(i), includeVariables, allowDiscretion);
				if(i != (children.size() - 1))
					height += NODE_SPACING;
			}
		}
		
		return height;
	}
	
	public static int nodeDisplayHeight(@Nullable TreeNode<?> nodeIn, boolean includeVariables)
	{
		if(nodeIn == null)
			return 13 + 11;
		
		int variables = nodeIn.getSubType().variableSet().size();
		
		// Type
		int height = 13;
		
		// Subtype
		height += (variables == 0 ? 15 : 11);
		
		// Variables
		if(variables > 0 && includeVariables)
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
	
	public static void drawTintedTexture(Identifier texture, DrawContext context, int x, int y, int uv0, int uv1, int width, int height, int red, int green, int blue)
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
	
	public static List<Pair<WhiteboardRef, Optional<INodeValue>>> getSortedVariables(TreeNode<?> node)
	{
		List<Pair<WhiteboardRef, Optional<INodeValue>>> variablesToDisplay = Lists.newArrayList();
		Map<WhiteboardRef, INodeInput> variableSet = node.getSubType().variableSet();
		for(WhiteboardRef input : variableSet.keySet())
		{
			INodeValue value = node.variable(input);
			variablesToDisplay.add(new Pair<>(input, value == null ? Optional.empty() : Optional.of(value)));
		}
		
		// Sort variables by input name
		variablesToDisplay.sort(new Comparator<>()
		{
			public int compare(Pair<WhiteboardRef, Optional<INodeValue>> o1, Pair<WhiteboardRef, Optional<INodeValue>> o2)
			{
				boolean optional1 = variableSet.get(o1.getLeft()).isOptional();
				boolean optional2 = variableSet.get(o2.getLeft()).isOptional();
				if(optional1 != optional2)
					return optional1 && !optional2 ? 1 : !optional1 && optional2 ? -1 : 0;
				return WhiteboardRef.REF_SORT.compare(o1.getLeft(), o2.getLeft());
			}
		});
		return variablesToDisplay;
	}
	
	public static enum NodeDisplay implements StringIdentifiable
	{
		NEVER,
		HOVERED,
		ALWAYS;
		
		public String asString() { return name().toLowerCase(); }
		
		@Nullable
		public static NodeDisplay fromString(String nameIn)
		{
			for(NodeDisplay type : values())
				if(nameIn.equals(type.asString()))
					return type;
			return null;
		}
	}
	
	public static enum NodeRenderFlags
	{
		TYPE,
		SUBTYPE,
		VARIABLES,
		CHILDREN;
		
		public static final EnumSet<NodeRenderFlags> ALL = EnumSet.allOf(NodeRenderFlags.class);
		public static final EnumSet<NodeRenderFlags> SOLO = EnumSet.of(TYPE, SUBTYPE, VARIABLES);
	}
}
