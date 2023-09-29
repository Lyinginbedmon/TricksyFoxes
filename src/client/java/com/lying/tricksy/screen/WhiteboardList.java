package com.lying.tricksy.screen;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.reference.Reference;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class WhiteboardList extends ElementListWidget<WhiteboardList.ReferenceEntry>
{
	public static final Identifier BOARD_TEXTURES = new Identifier(Reference.ModInfo.MOD_ID, "textures/gui/whiteboard.png");
	public static final Identifier SLICE_TEXTURE = new Identifier(Reference.ModInfo.MOD_ID, "textures/gui/whiteboard_slice.png");
	private final WhiteboardScreen parent;
	
	public WhiteboardList(WhiteboardScreen screen, int width, int height, int top, int bottom)
	{
		super(WhiteboardScreen.mc, width, height, top, bottom, 25);
		this.parent = screen;
		this.setRenderBackground(false);
		this.setRenderHorizontalShadows(false);
	}
	
	public int getRowWidth() { return this.width - 20; }
	
	public int getScrollbarPositionX() { return this.left + this.width - 5; }
	
	public void setEntries(Map<WhiteboardRef, IWhiteboardObject<?>> entries)
	{
		clearEntries();
		List<WhiteboardRef> keySet = Lists.newArrayList();
		keySet.addAll(entries.keySet());
		keySet.sort(WhiteboardRef.REF_SORT);
		
		keySet.forEach((ref) -> addEntry(new ReferenceEntry(ref, entries.get(ref), this)));
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		ReferenceEntry entry = getEntryAtPosition(mouseX, mouseY);
		if(entry == null)
			return false;
		else
			return entry.mouseClicked(mouseX, mouseY, button);
	}
	
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		float v2 = (float)this.height / 16F;
		
		int x1 = this.left - 25;
		int x2 = this.right + 25;
		
		RenderSystem.setShaderTexture(0, SLICE_TEXTURE);
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
	
	public class ReferenceEntry extends ElementListWidget.Entry<ReferenceEntry>
	{
		private static final MinecraftClient mc = MinecraftClient.getInstance();
		private final WhiteboardRef reference;
		@Nullable
		private final IWhiteboardObject<?> valueSnapshot;
		
		private final ButtonWidget deleteButton;
		private int ticksHovered = 0;
		
		public ReferenceEntry(WhiteboardRef referenceIn, @Nullable IWhiteboardObject<?> valueIn, WhiteboardList parent)
		{
			this.reference = referenceIn;
			this.valueSnapshot = valueIn;
			this.deleteButton = ButtonWidget.builder(Text.literal("X"), button -> 
			{
				WhiteboardList.this.parent.deleteReference(referenceIn);
				parent.removeEntry(this);
			}).dimensions(0, 0, 16, 16).build();
			this.deleteButton.active = this.deleteButton.visible = reference.boardType() != BoardType.CONSTANT && !reference.uncached();
		}
		
		public List<? extends Element> children()
		{
			return ImmutableList.of(deleteButton);
		}
		
		public List<? extends Selectable> selectableChildren()
		{
			return ImmutableList.of(deleteButton);
		}
		
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta)
		{
			context.drawTexture(BOARD_TEXTURES, x, y, 0, 0, 180, 25);
			
			NodeRenderUtils.renderReference(reference, context, mc.textRenderer, x, y + 3, 150, true, false);
			if(reference.boardType() == BoardType.CONSTANT)
				return;
			
			if(valueSnapshot != null)
			{
				List<Text> description = valueSnapshot.describe();
				
				if(hovered && description.size() > 1)
					ticksHovered++;
				else
					ticksHovered = 0;
				
				Text draw = description.get(Math.floorDiv(ticksHovered, Reference.Values.TICKS_PER_SECOND)%description.size());
				context.drawText(mc.textRenderer, draw, x + (150 - 8 - mc.textRenderer.getWidth(draw)) / 2, y + 15, 0x808080, false);
			}
			
			if(this.deleteButton.visible)
			{
				this.deleteButton.setX(x + 180 - deleteButton.getWidth());
				this.deleteButton.setY(y + (entryHeight - deleteButton.getHeight()) / 2);
				this.deleteButton.render(context, mouseX, mouseY, tickDelta);
			}
		}
	}
}
