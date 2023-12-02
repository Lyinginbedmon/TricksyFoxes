package com.lying.tricksy.entity.ai.node.handler;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.INodeValue;
import com.lying.tricksy.entity.ai.node.INodeValue.Type;
import com.lying.tricksy.entity.ai.node.INodeValue.WhiteboardValue;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.init.TFObjType;

import net.minecraft.entity.mob.PathAwareEntity;

public abstract class GetterHandlerTyped<T> extends GetterHandlerUntyped
{
	private final TFObjType<T> type;
	
	public GetterHandlerTyped(TFObjType<T> typeIn)
	{
		super(new TFObjType[] {typeIn});
		this.type = typeIn;
	}
	
	public <N extends PathAwareEntity & ITricksyMob<?>> Result doTick(N tricksy, LocalWhiteboard<N> local, GlobalWhiteboard global, LeafNode parent)
	{
		INodeValue target = parent.getInput(entry);
		if(target.type() != Type.WHITEBOARD)
			return Result.FAILURE;
		WhiteboardRef dest = ((WhiteboardValue)target).assignment();
		if(dest == null)
			return Result.FAILURE;
		
		IWhiteboardObject<T> result = getTypedResult(tricksy, local, global, parent);
		if(result == null || result.isEmpty() || result.size() == 0)
		{
			local.setValue(dest, type.blank());
			return Result.FAILURE;
		}
		
		local.setValue(dest, result);
		return Result.SUCCESS;
	}
	
	@Nullable
	public abstract <N extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<T> getTypedResult(N tricksy, LocalWhiteboard<N> local, GlobalWhiteboard global, LeafNode parent);
	
	@Nullable
	public <N extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<?> getResult(N tricksy, LocalWhiteboard<N> local, GlobalWhiteboard global, LeafNode parent)
	{
		return TFObjType.EMPTY.blank();
	}
}
