package com.lying.tricksy.screen.subscreen;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.screen.NodeScreen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ReferencesScreen extends NodeSubScreen
{
	private ReferenceList referenceList;
	private BoardList boardList;
	
	private BoardType boardDisplayed = BoardType.LOCAL;
	
	private Map<BoardType, Map<WhiteboardRef, IWhiteboardObject<?>>> availableValues = new HashMap<>();
	
	private Optional<CreateStaticScreen> staticScreen = Optional.empty();
	
	public ReferencesScreen(NodeScreen parentIn)
	{
		super(parentIn);
	}
	
	protected void init()
	{
		clearChildren();
		staticScreen = Optional.empty();
		
		addDrawableChild(referenceList = new ReferenceList(150, this.height, 0, this.height));
		referenceList.setLeftPos(this.width - 150);
		
		addDrawableChild(boardList = new BoardList(70, this.height, 0, this.height, 20, this));
		boardList.setLeftPos(referenceList.getRowLeft() - 67);
		
		addDrawableChild(ButtonWidget.builder(Text.literal("Static"), (button) -> 
		{
			CreateStaticScreen screen = new CreateStaticScreen(this.parent, this);
			screen.init(this.client, this.width, this.height);
			this.staticScreen = Optional.of(screen);
		}).dimensions(this.width / 2 - 20, this.height - 30, 40, 20).build());
		
		setBoard(BoardType.LOCAL);
	}
	
	private void populateReferences()
	{
		availableValues.clear();
		for(BoardType type : BoardType.values())
			availableValues.put(type, parent.getScreenHandler().getMatches(parent.targetInputPred(), type));
		
		this.boardList.refreshEntries();
	}
	
	public void setBoard(BoardType board)
	{
		populateReferences();
		// If we've somehow tried to display a board we don't have any values for, reset
		if(!hasValuesFor(board))
			board = availableValues.keySet().toArray(new BoardType[0])[0];
		
		this.boardDisplayed = board;
		this.referenceList.setEntries(availableValues.get(board), parent);
	}
	
	public boolean hasValuesFor(BoardType board) { return !availableValues.getOrDefault(board, new HashMap<>()).isEmpty(); }
	
	public boolean isDisplaying(BoardType board) { return board == this.boardDisplayed; }
	
	public void closeStatic() { this.staticScreen = Optional.empty(); }
	
	public void tick()
	{
		super.tick();
		this.staticScreen.ifPresent(screen -> screen.tick());
	}
	
	@Override
	public boolean charTyped(char chr, int modifiers)
	{
		if(staticScreen.isPresent())
			return staticScreen.get().charTyped(chr, modifiers);
		
		return super.charTyped(chr, modifiers);
	}
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if(this.staticScreen.isPresent())
			return this.staticScreen.get().keyPressed(keyCode, scanCode, modifiers);
		
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	public boolean mouseClicked(double x, double y, int mouseKey)
	{
		if(this.staticScreen.isPresent())
			return this.staticScreen.get().mouseClicked(x, y, mouseKey);
		return super.mouseClicked(x, y, mouseKey);
	}
	
	public boolean mouseScrolled(double mouseX, double mouseY, double amount)
	{
		if(this.staticScreen.isPresent())
			return this.staticScreen.get().mouseScrolled(mouseX, mouseY, amount);
		return super.mouseScrolled(mouseX, mouseY, amount);
	}
	
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		if(!this.staticScreen.isPresent())
			super.render(context, mouseX, mouseY, delta);
	}
	
	public void doForegroundRendering(DrawContext context, int mouseX, int mouseY)
	{
		if(this.staticScreen.isPresent())
			this.staticScreen.get().render(context, mouseX, mouseY, 0F);
	}
}
