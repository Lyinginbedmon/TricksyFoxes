package com.lying.tricksy.screen.subscreen;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.screen.NodeRenderUtils;
import com.lying.tricksy.screen.NodeScreen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;

public class ReferenceList extends ElementListWidget<ReferenceList.ReferenceEntry>
{
	public ReferenceList(int width, int height, int top, int bottom, int itemHeight)
	{
		super(MinecraftClient.getInstance(), width, height, top, bottom, itemHeight);
		this.setRenderHeader(false, 0);
		this.setRenderHorizontalShadows(false);
		this.setRenderSelection(false);
	}
	
	public void setEntries(Map<WhiteboardRef, IWhiteboardObject<?>> group, NodeScreen parent)
	{
		this.clearEntries();
		for(WhiteboardRef ref : group.keySet())
			addEntry(new ReferenceEntry(ref, parent, width));
	}
	
	public int getRowWidth() { return this.width; }
	
	protected int getScrollbarPositionX() { return this.left + this.width - 4; }
	
	public class ReferenceEntry extends ElementListWidget.Entry<ReferenceEntry>
	{
		private static final MinecraftClient mc = MinecraftClient.getInstance();
		private final NodeScreen parent;
		private final WhiteboardRef reference;
		
		public ReferenceEntry(WhiteboardRef subTypeIn, NodeScreen parentIn, int width)
		{
			this.parent = parentIn;
			this.reference = subTypeIn;
		}
		
		public List<? extends Element> children()
		{
			return ImmutableList.of();
		}
		
		public List<? extends Selectable> selectableChildren() { return ImmutableList.of(); }
		
		public boolean mouseClicked(double mouseX, double mouseY, int button)
		{
			parent.currentNode.assign(parent.targetInputRef(), reference);
			return true;
		}
		
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta)
		{
			NodeRenderUtils.renderReference(reference, context, mc.textRenderer, x, y, entryWidth, true, false);
		}
	}
}
