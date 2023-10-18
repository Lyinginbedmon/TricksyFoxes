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
		for(BoardType type : BoardType.values())
			availableValues.put(type, parent.getScreenHandler().getMatches(parent.targetInputPred(), type));
		
		addDrawableChild(referenceList = new ReferenceList(150, this.height, 0, this.height, 20));
		referenceList.setLeftPos(this.width - 150);
		setBoard(BoardType.LOCAL);
		
		addDrawableChild(boardList = new BoardList(60, this.height, 0, this.height, 20, this));
		boardList.setLeftPos(referenceList.getRowLeft() - 60);
	}
	
	public void setBoard(BoardType board)
	{
		this.boardDisplayed = board;
		this.referenceList.setEntries(availableValues.get(board), parent);
	}
	
	public boolean hasValuesFor(BoardType board) { return !availableValues.get(board).isEmpty(); }
	
	public boolean isDisplaying(BoardType board) { return board == this.boardDisplayed; }
}
