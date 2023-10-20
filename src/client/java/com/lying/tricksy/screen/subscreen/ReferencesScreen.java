package com.lying.tricksy.screen.subscreen;

import java.util.HashMap;
import java.util.Map;

import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.screen.NodeScreen;

public class ReferencesScreen extends NodeSubScreen
{
	private ReferenceList referenceList;
	private BoardList boardList;
	
	private BoardType boardDisplayed = BoardType.LOCAL;
	
	private Map<BoardType, Map<WhiteboardRef, IWhiteboardObject<?>>> availableValues = new HashMap<>();
	
	public ReferencesScreen(NodeScreen parentIn)
	{
		super(parentIn);
	}
	
	protected void init()
	{
		clearChildren();
		addDrawableChild(referenceList = new ReferenceList(150, this.height, 0, this.height));
		referenceList.setLeftPos(this.width - 150);
		
		addDrawableChild(boardList = new BoardList(70, this.height, 0, this.height, 20, this));
		boardList.setLeftPos(referenceList.getRowLeft() - 67);
		
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
}
