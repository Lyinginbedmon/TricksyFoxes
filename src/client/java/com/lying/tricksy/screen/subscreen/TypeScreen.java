package com.lying.tricksy.screen.subscreen;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.lying.tricksy.entity.ai.node.NodeType;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.screen.NodeScreen;

import net.minecraft.client.gui.widget.ButtonWidget;

public class TypeScreen extends NodeSubScreen
{
	private final Map<NodeType<?>, ButtonWidget> buttonMap = new HashMap<>();
	
	public TypeScreen(NodeScreen parentIn)
	{
		super(parentIn);
	}
	
	public void tick()
	{
		for(Entry<NodeType<?>, ButtonWidget> entry : buttonMap.entrySet())
			entry.getValue().active = parent.currentNode.getType() != entry.getKey();
	}
	
	protected void init()
	{
		for(NodeType<?> type : new NodeType<?>[] {TFNodeTypes.CONTROL_FLOW, TFNodeTypes.DECORATOR, TFNodeTypes.CONDITION, TFNodeTypes.LEAF})
		{
			ButtonWidget part = ButtonWidget.builder(type.translatedName(), (button) -> 
			{
				if(parent.currentNode.isRoot())
					return;
				
				UUID uuid = parent.currentNode.getID();
				TreeNode<?> replacement = type.create(uuid);
				parent.currentNode.parent().replaceChild(uuid, replacement);
				parent.currentNode = replacement;
				parent.updateTreeRender();
			}).dimensions(10, 10 + children().size() * 20 + children().size(), 100, 20).build();
			addDrawableChild(part);
			buttonMap.put(type, part);
		}
	}
}
