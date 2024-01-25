package com.lying.tricksy.screen.subscreen;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.init.TFWhiteboards;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.screen.BranchLine;
import com.lying.tricksy.screen.NodeRenderUtils;
import com.lying.tricksy.screen.NodeScreen;
import com.lying.tricksy.screen.WhiteboardList;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;

public class ReferenceList extends ElementListWidget<ReferenceList.ReferenceEntry>
{
	public static final MinecraftClient mc = MinecraftClient.getInstance();
	private BranchLine leftLine;
	
	public ReferenceList(int width, int height, int top, int bottom)
	{
		super(MinecraftClient.getInstance(), width, height, top, bottom, 25);
		this.setRenderHeader(false, 0);
		this.setRenderHorizontalShadows(false);
		this.setRenderSelection(false);
		this.setRenderBackground(false);
		setLeftPos(0);
	}
	
	public void setEntries(Map<WhiteboardRef, IWhiteboardObject<?>> group, NodeScreen parent)
	{
		this.clearEntries();
		List<WhiteboardRef> keySet = Lists.newArrayList();
		keySet.addAll(group.keySet());
		keySet.sort(WhiteboardRef.REF_SORT);
		keySet.forEach((ref) -> addEntry(new ReferenceEntry(ref, group.get(ref), parent, width)));
	}
	
	public void setLeftPos(int left)
	{
		super.setLeftPos(left);
		Random rand = new Random(mc.player.getUuid().getLeastSignificantBits());
		int leftPos = left;
		leftLine = BranchLine.between(new Vec2f(leftPos, 0), new Vec2f(leftPos, this.height), rand, rand.nextBoolean() ? TFNodeTypes.ROSE_FLOWER : TFNodeTypes.GRAPE_FLOWER);
	}
	
	public int getRowWidth() { return this.width - 20; }
	
	protected int getScrollbarPositionX() { return this.right - 4; }
	
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		float v2 = (float)this.height / 16F;
		
		int x1 = this.left - 25;
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
	
	public class ReferenceEntry extends ElementListWidget.Entry<ReferenceEntry>
	{
		private static final MinecraftClient mc = MinecraftClient.getInstance();
		private final NodeScreen parent;
		private final WhiteboardRef reference;
		private final IWhiteboardObject<?> valueSnapshot;
		
		private int ticksHovered = 0;
		
		public ReferenceEntry(WhiteboardRef subTypeIn, @Nullable IWhiteboardObject<?> value, NodeScreen parentIn, int width)
		{
			this.parent = parentIn;
			this.reference = subTypeIn;
			this.valueSnapshot = value;
		}
		
		public List<? extends Element> children()
		{
			return ImmutableList.of();
		}
		
		public List<? extends Selectable> selectableChildren() { return ImmutableList.of(); }
		
		public boolean mouseClicked(double mouseX, double mouseY, int button)
		{
			parent.currentNode.assignInputRef(parent.targetIORef(), reference);
			return true;
		}
		
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta)
		{
			context.drawTexture(WhiteboardList.BOARD_TEXTURES, x, y, 0, 0, 180, 25);
			
			NodeRenderUtils.renderRefType(reference.type(), context, x + 12, y + 3, 8, 8);
			
			Text name = reference.displayName();
			int namePos = x + 10;
			namePos += ((entryWidth - 10) - mc.textRenderer.getWidth(name)) / 2;
			context.drawText(mc.textRenderer, reference.displayName(), namePos, y + 3, 0x404040, false);
			
			if(reference.boardType() != TFWhiteboards.CONSTANT && valueSnapshot != null)
			{
				List<Text> description = valueSnapshot.describe();
				if(description.isEmpty()) return;
				ticksHovered = (hovered && description.size() > 1) ? ticksHovered + 1 : 0;
				Text draw = description.get(Math.floorDiv(ticksHovered, Reference.Values.TICKS_PER_SECOND)%description.size());
				context.drawText(mc.textRenderer, draw, x + (150 - 8 - mc.textRenderer.getWidth(draw)) / 2, y + 15, 0x808080, false);
			}
		}
	}
}
