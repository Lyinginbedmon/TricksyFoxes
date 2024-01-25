package com.lying.tricksy.entity.ai.node.handler;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.api.entity.ai.INodeIOValue;
import com.lying.tricksy.api.entity.ai.INodeIOValue.Type;
import com.lying.tricksy.api.entity.ai.INodeIOValue.WhiteboardValue;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardManager;
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
	
	public <N extends PathAwareEntity & ITricksyMob<?>> Result doTick(N tricksy, WhiteboardManager<N> whiteboards, LeafNode parent)
	{
		INodeIOValue target = parent.getIO(entry);
		if(target.type() != Type.WHITEBOARD)
			return Result.FAILURE;
		WhiteboardRef dest = ((WhiteboardValue)target).assignment();
		if(dest == null || dest.boardType().isReadOnly())
			return Result.FAILURE;
		
		IWhiteboardObject<T> result = getTypedResult(tricksy, whiteboards, parent);
		if(result == null || result.isEmpty() || result.size() == 0)
		{
			whiteboards.get(dest.boardType()).setValue(dest, type.blank());
			return Result.FAILURE;
		}
		
		whiteboards.get(dest.boardType()).setValue(dest, result);
		return Result.SUCCESS;
	}
	
	@Nullable
	public abstract <N extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<T> getTypedResult(N tricksy, WhiteboardManager<N> whiteboards, LeafNode parent);
	
	@Nullable
	public <N extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<?> getResult(N tricksy, WhiteboardManager<N> whiteboards, LeafNode parent)
	{
		return TFObjType.EMPTY.blank();
	}
}
