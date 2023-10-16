package com.lying.tricksy.screen;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class WhiteboardScreen extends TricksyScreenBase
{
	// Button to view behaviour tree
	public ButtonWidget tree;
	
	// Whiteboard tabs
	public Map<BoardType, ButtonWidget> boardMap = new HashMap<>();
	
	private BoardType currentBoard = BoardType.LOCAL;
	
	private WhiteboardList list;
	
	public WhiteboardScreen(TricksyTreeScreenHandler handler, PlayerInventory inventory, Text title)
	{
		super(handler, inventory, title);
	}
	
	protected void init()
	{
		addSelectableChild(list = new WhiteboardList(this, 200, this.height, 28, this.height));
		list.setLeftPos((this.width - 200) / 2);
		setBoard(BoardType.LOCAL);
		
		addDrawableChild(tree = makeTexturedWidget((this.width / 2) + 34, 18, 48, 184, (button) -> 
		{
			client.setScreen(new TreeScreen(this.handler, this.playerInv, this.title));
		}));
		
		boardMap.put(BoardType.CONSTANT, makeBoardButton(BoardType.CONSTANT, 0));
		boardMap.put(BoardType.GLOBAL, makeBoardButton(BoardType.GLOBAL, 20));
		boardMap.put(BoardType.LOCAL, makeBoardButton(BoardType.LOCAL, 40));
		boardMap.values().forEach((button) -> addDrawableChild(button));
		manageBoardButtons();
		this.list.setRandSeed(this.player.getUuid().getLeastSignificantBits());
	}
	
	private ButtonWidget makeBoardButton(BoardType board, int y)
	{
		return ButtonWidget.builder(board.translate(), (button) -> 
		{
			WhiteboardScreen screen = (WhiteboardScreen)mc.currentScreen;
			screen.setBoard(board);
			screen.manageBoardButtons();
		}).dimensions((this.width - 200) / 2 - 60, 60 + y, 60, 20).build();
	}
	
	private void manageBoardButtons()
	{
		for(Entry<BoardType, ButtonWidget> entry : boardMap.entrySet())
			entry.getValue().active = entry.getKey() != currentBoard;
	}
	
	public void setBoard(BoardType board)
	{
		this.currentBoard = board;
		this.list.setEntries(this.handler.getEntriesOnBoard(board));
		this.list.setScrollAmount(0D);
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		if(this.list.mouseClicked(mouseX, mouseY, button))
			return true;
		else
			return super.mouseClicked(mouseX, mouseY, button);
	}
	
	public void deleteReference(WhiteboardRef targetRef)
	{
		if(targetRef == null || targetRef.boardType() == BoardType.CONSTANT)
			return;
		
		this.handler.markForDeletion(targetRef);
	}
	
	protected void drawForeground(DrawContext context, int mouseX, int mouseY)
	{
		NodeRenderUtils.drawTextures(context, (this.width - 200) / 2, 2, 0, 68, 200, 26, 255, 255, 255);
		context.drawText(textRenderer, this.title, (this.width - this.textRenderer.getWidth(this.title)) / 2, 2 + (26 - this.textRenderer.fontHeight) / 2, 0x404040, false);
		this.tree.render(context, mouseX, mouseY, 0F);
	}
	
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY)
	{
		renderBackground(context);
		this.list.render(context, mouseX, mouseY, delta);
		
	}
}
