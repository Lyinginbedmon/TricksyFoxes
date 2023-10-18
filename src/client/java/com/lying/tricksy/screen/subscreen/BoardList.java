package com.lying.tricksy.screen.subscreen;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;

public class BoardList extends ElementListWidget<BoardList.BoardEntry>
{
	public BoardList(int width, int height, int top, int bottom, int itemHeight, ReferencesScreen parent)
	{
		super(MinecraftClient.getInstance(), width, height, top, bottom, itemHeight);
		this.setRenderHeader(false, 0);
		this.setRenderHorizontalShadows(false);
		this.setRenderSelection(false);
		this.setRenderBackground(false);
		
		for(BoardType board : new BoardType[] {BoardType.CONSTANT, BoardType.GLOBAL, BoardType.LOCAL})
			if(parent.hasValuesFor(board))
				addEntry(new BoardEntry(board, parent, width));
	}
	
	public int getRowWidth() { return this.width; }
	
	protected int getScrollbarPositionX() { return this.left + this.width - 4; }
	
	public class BoardEntry extends ElementListWidget.Entry<BoardEntry>
	{
		private final ReferencesScreen parent;
		private final BoardType board;
		
		private final ButtonWidget button;
		
		public BoardEntry(BoardType boardIn, ReferencesScreen parentIn, int width)
		{
			this.parent = parentIn;
			this.board = boardIn;
			
			this.button = ButtonWidget.builder(boardIn.translate(), (button) -> parent.setBoard(board)).dimensions(0, 0, width, 20).build();
		}
		
		public List<? extends Element> children() { return ImmutableList.of(); }
		
		public List<? extends Selectable> selectableChildren() { return ImmutableList.of(); }
		
		public boolean mouseClicked(double mouseX, double mouseY, int button)
		{
			return this.button.mouseClicked(mouseX, mouseY, button);
		}
		
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta)
		{
			this.button.active = !parent.isDisplaying(board);
			this.button.setPosition(x + (entryWidth - button.getWidth()) / 2, y);
			this.button.render(context, mouseX, mouseY, tickDelta);
		}
	}
}
