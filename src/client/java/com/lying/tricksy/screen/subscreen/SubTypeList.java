package com.lying.tricksy.screen.subscreen;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ai.node.subtype.ISubtypeGroup;
import com.lying.tricksy.entity.ai.node.subtype.NodeSubType;
import com.lying.tricksy.screen.NodeScreen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;

public class SubTypeList extends ElementListWidget<SubTypeList.SubTypeEntry>
{
	public SubTypeList(int width, int height, int top, int bottom, int itemHeight)
	{
		super(MinecraftClient.getInstance(), width, height, top, bottom, itemHeight);
		this.setRenderHeader(false, 0);
		this.setRenderHorizontalShadows(false);
		this.setRenderSelection(false);
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
	
	public class SubTypeEntry extends ElementListWidget.Entry<SubTypeEntry>
	{
		private final NodeScreen parent;
		private final NodeSubType<?> subType;
		
		private final ButtonWidget button;
		
		public SubTypeEntry(NodeSubType<?> subTypeIn, NodeScreen parentIn, int width)
		{
			this.parent = parentIn;
			this.subType = subTypeIn;
			
			this.button = ButtonWidget.builder(subTypeIn.translatedName(), (button) -> parent.currentNode.setSubType(subType.getRegistryName())).dimensions(0, 0, width, 20).build();
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
			this.button.setPosition(x + (entryWidth - button.getWidth()) / 2, y);
			this.button.render(context, mouseX, mouseY, tickDelta);
		}
	}
}
