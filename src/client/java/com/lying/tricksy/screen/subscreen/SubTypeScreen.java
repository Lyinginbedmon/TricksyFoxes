package com.lying.tricksy.screen.subscreen;

import com.lying.tricksy.entity.ai.node.NodeType;
import com.lying.tricksy.entity.ai.node.subtype.ISubtypeGroup;
import com.lying.tricksy.screen.NodeScreen;

import net.minecraft.util.Identifier;

public class SubTypeScreen extends NodeSubScreen
{
	private SubTypeList subTypeList;
	private SubTypeGroupList groupList;
	
	private ISubtypeGroup<?> groupDisplayed = null;
	
	public SubTypeScreen(NodeScreen parentIn)
	{
		super(parentIn);
	}
	
	protected void init()
	{
		addDrawableChild(subTypeList = new SubTypeList(150, this.height, 0, this.height, 20));
		subTypeList.setLeftPos(this.width - 150);
		
		addDrawableChild(groupList = new SubTypeGroupList(50, this.height, 0, this.height, 22));
		groupList.setLeftPos(subTypeList.getRowLeft() - 50);
		
		NodeType<?> type = parent.currentNode.getType();
		groupList.setEntries(type, this);
		
		Identifier currentGroup = type.getGroupOf(parent.currentNode.getSubType().getRegistryName());
		for(ISubtypeGroup<?> group : type.groups())
			if(group.getRegistryName().equals(currentGroup))
			{
				setGroup(group);
				break;
			}
		
		if(groupDisplayed == null)
			setGroup(type.groups().get(0));
	}
	
	public void setGroup(ISubtypeGroup<?> group)
	{
		this.groupDisplayed = group;
		this.subTypeList.setEntries(group, parent);
	}
	
	public boolean isDisplaying(ISubtypeGroup<?> group) { return group == this.groupDisplayed; }
}
