package com.lying.tricksy.screen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.network.DeleteReferencePacket;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class WhiteboardScreen extends HandledScreen<TreeScreenHandler>
{
	public static final MinecraftClient mc = MinecraftClient.getInstance();
	private final PlayerEntity player;
	private final PlayerInventory playerInv;
	
	// Button to view behaviour tree
	public ButtonWidget tree;
	// Button to delete a cached value
	public ButtonWidget delete;
	// Whiteboard tabs
	public Map<BoardType, ButtonWidget> boardMap = new HashMap<>();
	
	private List<WhiteboardRef> references = Lists.newArrayList();
	private WhiteboardRef targetRef = null;
	private BoardType currentBoard = BoardType.LOCAL;
	
	public WhiteboardScreen(TreeScreenHandler handler, PlayerInventory inventory, Text title)
	{
		super(handler, inventory, title);
		this.player = inventory.player;
		this.playerInv = inventory;
	}
	
	protected void init()
	{
		addDrawableChild(tree = ButtonWidget.builder(Text.literal("Tree"), (button) -> 
		{
			client.setScreen(new TreeScreen(this.handler, this.playerInv, this.title));
		}).dimensions(0, this.height - 16, 50, 16).build());
		addDrawableChild(delete = ButtonWidget.builder(Text.literal("X"), (button) -> 
		{
			((WhiteboardScreen)mc.currentScreen).deleteCurrentRef();
		}).dimensions(0, 0, 16, 16).build());
		
		boardMap.put(BoardType.CONSTANT, makeBoardButton(BoardType.CONSTANT, 0));
		boardMap.put(BoardType.GLOBAL, makeBoardButton(BoardType.GLOBAL, 20));
		boardMap.put(BoardType.LOCAL, makeBoardButton(BoardType.LOCAL, 40));
		boardMap.values().forEach((button) -> addDrawableChild(button));
		manageBoardButtons();
	}
	
	private ButtonWidget makeBoardButton(BoardType board, int y)
	{
		return ButtonWidget.builder(board.translate(), (button) -> 
		{
			WhiteboardScreen screen = (WhiteboardScreen)mc.currentScreen;
			screen.setBoard(board);
			screen.manageBoardButtons();
		}).dimensions((this.width - 200) / 2, 60 + y, 60, 16).build();
	}
	
	private void manageBoardButtons()
	{
		for(Entry<BoardType, ButtonWidget> entry : boardMap.entrySet())
			entry.getValue().active = entry.getKey() != currentBoard;
	}
	
	public void setBoard(BoardType board)
	{
		this.currentBoard = board;
	}
	
	public void deleteCurrentRef()
	{
		if(targetRef == null)
			return;
		
		this.handler.removeRef(targetRef);
		DeleteReferencePacket.send(player, this.handler.tricksyUUID(), targetRef);
		targetRef = null;
	}
	
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY)
	{
		renderBackground(context);
		
		int listStart = 50;
		int slot = Math.floorDiv(mouseY - listStart, 16);
		if(Math.abs((this.width / 2) - mouseX) > 100)
			slot = -1;
		
		references.clear();
		references.addAll(getScreenHandler().getMatches((ref) -> ref.boardType() == currentBoard));
		references.sort(WhiteboardRef.REF_SORT);
		for(int i=0; i<references.size(); i++)
			NodeRenderUtils.renderReference(references.get(i), context, textRenderer, (this.width - 150) / 2, listStart + i * 16 + (16 - textRenderer.fontHeight) / 2, 150, true);
		
		if(slot >= 0 && slot < references.size() && currentBoard != BoardType.CONSTANT)
		{
			this.targetRef = references.get(slot);
			delete.visible = true;
			delete.active = !references.get(slot).uncached();
			delete.setPosition((this.width / 2) + 80, listStart + slot * 16);
		}
		else
			delete.visible = delete.active = false;
	}
}
