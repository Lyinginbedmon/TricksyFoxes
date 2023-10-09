package com.lying.tricksy.entity.ai.node.handler;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFObjType;

import net.minecraft.entity.mob.PathAwareEntity;

public abstract class GetterHandler<T> implements NodeTickHandler<LeafNode>
{
	private final WhiteboardRef entry;
	private final TFObjType<T> type;
	
	private final Map<WhiteboardRef, INodeInput> variableSet = new HashMap<>();
	
	public GetterHandler(TFObjType<T> typeIn)
	{
		this.type = typeIn;
		this.entry = new WhiteboardRef("target_reference", type).displayName(CommonVariables.translate("ref_target"));
		
		this.variableSet.put(entry, INodeInput.makeInput((var) -> var.type() == type && var.boardType() == BoardType.LOCAL && !var.uncached()));
		addVariables(this.variableSet);
	}
	
	public Map<WhiteboardRef, INodeInput> variableSet() { return this.variableSet; }
	
	public <N extends PathAwareEntity & ITricksyMob<?>> Result doTick(N tricksy, LocalWhiteboard<N> local, GlobalWhiteboard global, LeafNode parent)
	{
		WhiteboardRef dest = parent.variable(entry);
		IWhiteboardObject<T> result = getResult(tricksy, local, global, parent);
		if(result == null || result.isEmpty())
		{
			local.setValue(dest, type.blank());
			return Result.FAILURE;
		}
		
		local.setValue(dest, result);
		return Result.SUCCESS;
	}
	
	public abstract void addVariables(Map<WhiteboardRef, INodeInput> set);
	
	@Nullable
	public abstract <N extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<T> getResult(N tricksy, LocalWhiteboard<N> local, GlobalWhiteboard global, LeafNode parent);
}
