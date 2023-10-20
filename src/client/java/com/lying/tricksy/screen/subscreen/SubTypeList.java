package com.lying.tricksy.screen.subscreen;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.joml.Matrix4f;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ai.node.subtype.ISubtypeGroup;
import com.lying.tricksy.entity.ai.node.subtype.NodeSubType;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.screen.BranchLine;
import com.lying.tricksy.screen.NodeScreen;
import com.lying.tricksy.screen.SubTypeButton;
import com.lying.tricksy.screen.WhiteboardList;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Vec2f;

public class SubTypeList extends ElementListWidget<SubTypeList.SubTypeEntry>
{
	public static final MinecraftClient mc = MinecraftClient.getInstance();
	private BranchLine leftLine;
	
	public SubTypeList(int width, int height, int top, int bottom, int itemHeight)
	{
		super(MinecraftClient.getInstance(), width, height, top, bottom, itemHeight);
		this.setRenderHeader(false, 0);
		this.setRenderHorizontalShadows(false);
		this.setRenderSelection(false);
		this.setRenderBackground(false);
	}
	
	public void setLeftPos(int left)
	{
		super.setLeftPos(left);
		Random rand = new Random(mc.player.getUuid().getLeastSignificantBits());
		int leftPos = left - 8;
		leftLine = BranchLine.between(new Vec2f(leftPos, 0), new Vec2f(leftPos, this.height), rand, rand.nextBoolean() ? TFNodeTypes.ROSE_FLOWER : TFNodeTypes.GRAPE_FLOWER);
	}
	
	public void setEntries(ISubtypeGroup<?> group, NodeScreen parent)
	{
		this.clearEntries();
		
		Map<String, NodeSubType<?>> subtypeMap = new HashMap<>();
		List<String> names = Lists.newArrayList();
		group.getSubtypes().forEach((subtype) -> 
		{
			String name = subtype.translatedName().getString();
			subtypeMap.put(name, subtype);
			names.add(name);
		});
		
		names.sort(Comparator.naturalOrder());
		names.forEach((name) -> addEntry(new SubTypeEntry(subtypeMap.get(name), parent, this.width)));
	}
	
	public int getRowWidth() { return this.width; }
	
	protected int getScrollbarPositionX() { return this.left + this.width - 4; }
	
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		float v2 = (float)this.height / 16F;
		
		int x1 = this.left - 33;
		int x2 = x1 + 250;
		
		RenderSystem.setShaderTexture(0, WhiteboardList.SLICE_TEXTURE);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.enableBlend();
        Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
	        bufferBuilder.vertex(matrix4f, x1, 0, 0).texture(0F, 0F).next();
	        bufferBuilder.vertex(matrix4f, x1, this.bottom, 0).texture(0F, v2).next();
	        bufferBuilder.vertex(matrix4f, x2, this.bottom, 0).texture(1F, v2).next();
	        bufferBuilder.vertex(matrix4f, x2, 0, 0).texture(1F, 0F).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
        
		super.render(context, mouseX, mouseY, delta);
	}
	
	protected void renderDecorations(DrawContext context, int mouseX, int mouseY)
	{
		leftLine.render(context);
	}
	
	public class SubTypeEntry extends ElementListWidget.Entry<SubTypeEntry>
	{
		private final NodeScreen parent;
		private final NodeSubType<?> subType;
		
		private final ButtonWidget button;
		
		public SubTypeEntry(NodeSubType<?> subTypeIn, NodeScreen parentIn, int width)
		{
			this.parent = parentIn;
			this.subType = subTypeIn;
			
			this.button = new SubTypeButton(0, 0, subTypeIn.translatedName(), (button) -> parent.currentNode.setSubType(subType.getRegistryName()));
			this.button.setTooltip(Tooltip.of(this.subType.description()));
		}
		
		public List<? extends Element> children()
		{
			return ImmutableList.of();
		}
		
		public List<? extends Selectable> selectableChildren() { return ImmutableList.of(); }
		
		public boolean mouseClicked(double mouseX, double mouseY, int button)
		{
			this.button.mouseClicked(mouseX, mouseY, button);
			return true;
		}
		
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta)
		{
			this.button.active = !parent.currentNode.getSubType().getRegistryName().equals(subType.getRegistryName());
			if(this.button.active)
				this.button.setTooltip(Tooltip.of(this.subType.description()));
			else
				this.button.setTooltip(null);
			this.button.setPosition(x, y);
			this.button.render(context, mouseX, mouseY, tickDelta);
		}
	}
}
