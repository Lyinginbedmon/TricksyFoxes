package com.lying.tricksy.screen.subscreen;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.screen.SubTypeButton;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;

public class ObjTypeList extends ElementListWidget<ObjTypeList.ObjTypeEntry>
{
	private static final List<TFObjType<?>> STATIC_TYPES = List.of(TFObjType.BOOL, TFObjType.INT, TFObjType.BLOCK);
	public static final MinecraftClient mc = MinecraftClient.getInstance();
//	private BranchLine leftLine;
	
	public ObjTypeList(int width, int height, int top, int bottom)
	{
		super(MinecraftClient.getInstance(), width, height, top, bottom, 25);
//		this.setRenderHeader(false, 0);
//		this.setRenderHorizontalShadows(false);
//		this.setRenderSelection(false);
//		this.setRenderBackground(false);
	}
	
	public void setLeftPos(int left)
	{
		super.setLeftPos(left);
//		Random rand = new Random(mc.player.getUuid().getLeastSignificantBits());
//		int leftPos = left - 8;
//		leftLine = BranchLine.between(new Vec2f(leftPos, 0), new Vec2f(leftPos, this.height), rand, rand.nextBoolean() ? TFNodeTypes.ROSE_FLOWER : TFNodeTypes.GRAPE_FLOWER);
	}
	
	public void setEntries(Predicate<WhiteboardRef> condition, CreateStaticScreen parent)
	{
		clearEntries();
		
		Map<String, TFObjType<?>> subtypeMap = new HashMap<>();
		List<String> names = Lists.newArrayList();
		
		STATIC_TYPES.forEach((type) -> 
		{
			boolean pass = false;
			for(BoardType board : BoardType.values())
			{
				WhiteboardRef ref = new WhiteboardRef("test", type, board);
				if(condition.test(ref))
				{
					pass = true;
					break;
				}
			}
			if(!pass)
				return;
			
			String name = type.translated().getString();
			subtypeMap.put(name, type);
			names.add(name);
		});
		
		names.sort(Comparator.naturalOrder());
		names.forEach((name) -> addEntry(new ObjTypeEntry(subtypeMap.get(name), parent, this.width)));
	}
	
	public int getRowWidth() { return this.width; }
	
//	protected int getScrollbarPositionX() { return this.left + this.width - 4; }
	
//	public void render(DrawContext context, int mouseX, int mouseY, float delta)
//	{
//		float v2 = (float)this.height / 16F;
//		
//		int x1 = this.left - 33;
//		int x2 = x1 + 250;
//		
//		RenderSystem.setShaderTexture(0, WhiteboardList.SLICE_TEXTURE);
//        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
//        RenderSystem.enableBlend();
//        Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
//        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
//        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
//	        bufferBuilder.vertex(matrix4f, x1, 0, 0).texture(0F, 0F).next();
//	        bufferBuilder.vertex(matrix4f, x1, this.bottom, 0).texture(0F, v2).next();
//	        bufferBuilder.vertex(matrix4f, x2, this.bottom, 0).texture(1F, v2).next();
//	        bufferBuilder.vertex(matrix4f, x2, 0, 0).texture(1F, 0F).next();
//        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
//        RenderSystem.disableBlend();
//        
//		super.render(context, mouseX, mouseY, delta);
//	}
	
	protected void renderDecorations(DrawContext context, int mouseX, int mouseY)
	{
//		leftLine.render(context);
	}
	
	public class ObjTypeEntry extends ElementListWidget.Entry<ObjTypeEntry>
	{
		private final CreateStaticScreen parent;
		private final TFObjType<?> subType;
		
		private final ButtonWidget button;
		
		public ObjTypeEntry(TFObjType<?> objTypeIn, CreateStaticScreen parentIn, int width)
		{
			this.parent = parentIn;
			this.subType = objTypeIn;
			
			this.button = new SubTypeButton(0, 0, objTypeIn.translated(), (button) -> { this.parent.openDialog(objTypeIn); });
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
			this.button.active = !parent.currentType.registryName().equals(subType.registryName());
			this.button.setPosition(x, y);
			this.button.render(context, mouseX, mouseY, tickDelta);
		}
	}
}
