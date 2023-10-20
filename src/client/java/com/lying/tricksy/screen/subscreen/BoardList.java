package com.lying.tricksy.screen.subscreen;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;
import com.lying.tricksy.screen.BoardButton;
import com.lying.tricksy.screen.WhiteboardList;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.util.Identifier;

public class BoardList extends ElementListWidget<BoardList.BoardEntry>
{
	private final ReferencesScreen parent;
	
	public BoardList(int width, int height, int top, int bottom, int itemHeight, ReferencesScreen parentIn)
	{
		super(MinecraftClient.getInstance(), width, height, top, bottom, itemHeight);
		this.parent = parentIn;
		
		this.setRenderHeader(false, 0);
		this.setRenderHorizontalShadows(false);
		this.setRenderSelection(false);
		this.setRenderBackground(false);
		
		refreshEntries();
	}
	
	public void refreshEntries()
	{
		this.clearEntries();
		for(BoardType board : new BoardType[] {BoardType.CONSTANT, BoardType.GLOBAL, BoardType.LOCAL})
			if(parent.hasValuesFor(board))
				addEntry(new BoardEntry(board, parent, width));
	}
	
	public int getRowWidth() { return this.width; }
	
	protected int getScrollbarPositionX() { return this.right - 4; }
	
	public class BoardEntry extends ElementListWidget.Entry<BoardEntry>
	{
		public static final MinecraftClient mc = MinecraftClient.getInstance();
		public static final Identifier TEXTURE = WhiteboardList.BOARD_TEXTURES;
		
		private final ReferencesScreen parent;
		private final BoardType board;
		
		private final BoardButton button;
		
		public BoardEntry(BoardType boardIn, ReferencesScreen parentIn, int width)
		{
			this.parent = parentIn;
			this.board = boardIn;
			
			this.button = new BoardButton(0, 0, board.translate(), (button) -> parent.setBoard(board));
			this.button.active = !parent.isDisplaying(board);
		}
		
		public List<? extends Element> children() { return ImmutableList.of(); }
		
		public List<? extends Selectable> selectableChildren() { return ImmutableList.of(); }
		
		public boolean mouseClicked(double mouseX, double mouseY, int button)
		{
			this.button.mouseClicked(mouseX, mouseY, button);
			return true;
		}
		
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta)
		{
			this.button.active = !parent.isDisplaying(board);
			this.button.setPosition(x, y);
			this.button.render(context, mouseX, mouseY, tickDelta);
		}
	}
}
