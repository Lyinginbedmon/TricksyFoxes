package com.lying.tricksy.screen.subscreen;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ai.node.NodeType;
import com.lying.tricksy.entity.ai.node.subtype.ISubtypeGroup;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;

public class SubTypeGroupList extends ElementListWidget<SubTypeGroupList.GroupEntry>
{
	public SubTypeGroupList(int width, int height, int top, int bottom, int itemHeight)
	{
		super(MinecraftClient.getInstance(), width, height, top, bottom, itemHeight);
		this.setRenderHeader(false, 0);
		this.setRenderHorizontalShadows(false);
		this.setRenderSelection(false);
		this.setRenderBackground(false);
	}
	
	public void setEntries(NodeType<?> type, SubTypeScreen parent)
	{
		this.clearEntries();
		if(type.groups().size() > 1)
		{
			Map<String, ISubtypeGroup<?>> groupMap = new HashMap<>();
			List<String> names = Lists.newArrayList();
			type.groups().forEach((group) -> 
			{
				String name = group.displayName().getString();
				groupMap.put(name, group);
				names.add(name);
			});
			
			names.sort(Comparator.naturalOrder());
			names.forEach((name) -> addEntry(new GroupEntry(groupMap.get(name), parent, this.width)));
		}
	}
	
	public int getRowWidth() { return this.width; }
	
	protected int getScrollbarPositionX() { return this.left + this.width - 4; }
	
	public class GroupEntry extends ElementListWidget.Entry<GroupEntry>
	{
		private final SubTypeScreen parent;
		private final ISubtypeGroup<?> group;
		
		private final ButtonWidget button;
		
		public GroupEntry(ISubtypeGroup<?> groupIn, SubTypeScreen parentIn, int width)
		{
			this.parent = parentIn;
			this.group = groupIn;
			
			this.button = ButtonWidget.builder(groupIn.displayName(), (button) -> parent.setGroup(group)).dimensions(0, 0, width, 20).build();
		}
		
		public List<? extends Element> children()
		{
			return ImmutableList.of();
		}
		
		public List<? extends Selectable> selectableChildren() { return ImmutableList.of(); }
		
		public boolean mouseClicked(double mouseX, double mouseY, int button)
		{
			return this.button.mouseClicked(mouseX, mouseY, button);
		}
		
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta)
		{
			this.button.active = !parent.isDisplaying(group);
			this.button.setPosition(x + (entryWidth - button.getWidth()) / 2, y);
			this.button.render(context, mouseX, mouseY, tickDelta);
		}
	}
}
