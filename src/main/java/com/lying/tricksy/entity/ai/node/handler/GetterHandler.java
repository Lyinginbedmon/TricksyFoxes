package com.lying.tricksy.entity.ai.node.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.INodeValue;
import com.lying.tricksy.entity.ai.node.INodeValue.Type;
import com.lying.tricksy.entity.ai.node.INodeValue.WhiteboardValue;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.utility.Region;
import com.lying.tricksy.utility.RegionSphere;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;

public abstract class GetterHandler<T> implements NodeTickHandler<LeafNode>
{
	public static final INodeInput POS_OR_REGION = INodeInput.makeInput(INodeInput.ofType(TFObjType.BLOCK, false), new WhiteboardObjBlock(), LocalWhiteboard.SELF.displayName());
	
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
		INodeValue target = parent.variable(entry);
		WhiteboardRef dest = target.type() == Type.WHITEBOARD ? ((WhiteboardValue)target).assignment() : null;
		if(dest == null)
			return Result.FAILURE;
		
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
	
	/** Returns a provided Region or generates one around a provided position */
	public static <N extends PathAwareEntity & ITricksyMob<?>> Region getSearchArea(IWhiteboardObject<?> pos, IWhiteboardObject<Integer> range, N tricksy)
	{
		return getSearchArea(pos, range, tricksy, (mob) -> mob.getBlockPos());
	}
	
	/** Returns a provided Region or generates one around a provided position or calculated fallback */
	@Nullable
	public static <N extends PathAwareEntity & ITricksyMob<?>> Region getSearchArea(IWhiteboardObject<?> pos, IWhiteboardObject<Integer> range, N tricksy, Function<N,BlockPos> fallback)
	{
		if(pos.type() == TFObjType.REGION)
			return pos.as(TFObjType.REGION).get();
		
		BlockPos point = pos.size() == 0 ? fallback.apply(tricksy) : pos.as(TFObjType.BLOCK).get();
		return point == null ? null : new RegionSphere(point, range.get());
		
	}
}
