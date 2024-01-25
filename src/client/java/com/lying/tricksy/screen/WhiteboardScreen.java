package com.lying.tricksy.screen;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFWhiteboards;
import com.lying.tricksy.init.TFWhiteboards.BoardType;
import com.lying.tricksy.screen.subscreen.CreateRefScreen;
import com.lying.tricksy.screen.subscreen.NestedScreen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class WhiteboardScreen extends TricksyScreenBase implements INestedScreenProvider<WhiteboardScreen>
{
	// Button to view behaviour tree
	public ButtonWidget tree;
	
	// Whiteboard tabs
	public Map<BoardType, ButtonWidget> boardMap = new HashMap<>();
	
	private BoardType currentBoard = TFWhiteboards.LOCAL;
	
	private WhiteboardList list;
	
	private CreateRefScreen createDialog = null;
	private ButtonWidget createButton;
	
	public WhiteboardScreen(TricksyTreeScreenHandler handler, PlayerInventory inventory, Text title)
	{
		super(handler, inventory, title);
	}
	
	protected void init()
	{
		addSelectableChild(list = new WhiteboardList(this, 200, this.height, 28, this.height));
		list.setLeftPos((this.width - 200) / 2);
		setBoard(TFWhiteboards.LOCAL);
		
		addDrawableChild(tree = makeTexturedWidget((this.width / 2) + 34, 18, 48, 184, (button) -> 
		{
			client.setScreen(new TreeScreen(this.handler, this.playerInv, this.title));
		}));
		
		int y=0;
		for(BoardType type : TFWhiteboards.displayOrder())
			if(type.alwaysVisible() || getScreenHandler().hasEntriesOnBoard(type))
				boardMap.put(type, makeBoardButton(type, y++ * 20));
		boardMap.values().forEach((button) -> addDrawableChild(button));
		manageBoardButtons();
		this.list.setRandSeed(this.player.getUuid().getLeastSignificantBits());
		
		addDrawableChild(createButton = ButtonWidget.builder(Text.literal("+"), (button) -> openRefScreen()).dimensions((this.width / 2) + 75, this.height - 25, 20, 20).build());
	}
	
	public Optional<NestedScreen<WhiteboardScreen>> getSubScreen()
	{
		return this.createDialog == null ? Optional.empty() : Optional.of(this.createDialog);
	}
	
	public void closeSubScreen() { this.createDialog = null; }
	
	public void openRefScreen()
	{
		this.createDialog = new CreateRefScreen(this);
		initChild(client, width, height);
	}
	
	private ButtonWidget makeBoardButton(BoardType board, int y)
	{
		return new BoardButton((this.width - 200) / 2 - 53, 30 + y, board);
	}
	
	void manageBoardButtons()
	{
		for(Entry<BoardType, ButtonWidget> entry : boardMap.entrySet())
			entry.getValue().active = entry.getKey() != currentBoard;
	}
	
	public void setBoard(BoardType board)
	{
		this.currentBoard = board;
		updateList();
	}
	
	public void updateList()
	{
		this.list.setEntries(this.handler.getEntriesOnBoard(this.currentBoard));
		this.list.setScrollAmount(0D);
	}
	
	public void handledScreenTick()
	{
		super.handledScreenTick();
		this.createButton.active = this.createButton.visible = this.currentBoard == TFWhiteboards.LOCAL;
		tickChild();
	}
	
	@Override
	public boolean charTyped(char chr, int modifiers)
	{
		return childCharTyped(chr, modifiers) || super.charTyped(chr, modifiers);
	}
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		return childKeyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		return childMouseClicked(mouseX, mouseY, button) || this.list.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
	}
	
	public void deleteReference(WhiteboardRef targetRef)
	{
		if(targetRef == null || targetRef.boardType().isReadOnly())
			return;
		
		this.handler.markForDeletion(targetRef);
	}
	
	protected void drawForeground(DrawContext context, int mouseX, int mouseY)
	{
		NodeRenderUtils.drawTextures(context, (this.width - 200) / 2, 2, 0, 68, 200, 26, 255, 255, 255);
		context.drawText(textRenderer, this.title, (this.width - this.textRenderer.getWidth(this.title)) / 2, 2 + (26 - this.textRenderer.fontHeight) / 2, 0x404040, false);
		this.tree.render(context, mouseX, mouseY, 0F);
		
		renderChild(context, 0F, mouseX, mouseY);
	}
	
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY)
	{
		renderBackground(context);
		this.list.render(context, mouseX, mouseY, delta);
	}
}
