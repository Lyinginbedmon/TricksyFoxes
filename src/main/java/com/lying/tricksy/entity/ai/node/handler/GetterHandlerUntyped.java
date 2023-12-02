package com.lying.tricksy.entity.ai.node.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.INodeValue;
import com.lying.tricksy.entity.ai.node.INodeValue.Type;
import com.lying.tricksy.entity.ai.node.INodeValue.WhiteboardValue;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.utility.Region;
import com.lying.tricksy.utility.RegionSphere;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;

public abstract class GetterHandlerUntyped implements NodeTickHandler<LeafNode>
{
	public static final INodeInput POS_OR_REGION = INodeInput.makeInput(ref -> (ref.type() == TFObjType.BLOCK && !ref.isFilter()) || ref.type() == TFObjType.REGION, new WhiteboardObjBlock(), LocalWhiteboard.SELF.displayName());
	public static final INodeInput NUM_OR_POS = INodeInput.makeInput(ref -> ref.type() == TFObjType.INT || ref.type() == TFObjType.BLOCK);
	
	protected final WhiteboardRef entry;
	
	private final Map<WhiteboardRef, INodeInput> variableSet = new HashMap<>();
	
	public GetterHandlerUntyped(TFObjType<?>... typesIn)
	{
		if(typesIn == null || typesIn.length < 1)
			typesIn = new TFObjType[] {TFObjType.BOOL};
		this.entry = new WhiteboardRef("target_reference", typesIn[0]).displayName(CommonVariables.translate("ref_target"));
		
		// TODO Formalise output values as distinct from input values
		this.variableSet.put(entry, INodeInput.outputRefOnly(typesIn));
		addInputVariables(this.variableSet);
	}
	
	public Map<WhiteboardRef, INodeInput> inputSet() { return this.variableSet; }
	
	public <N extends PathAwareEntity & ITricksyMob<?>> Result doTick(N tricksy, LocalWhiteboard<N> local, GlobalWhiteboard global, LeafNode parent)
	{
		INodeValue target = parent.getInput(entry);
		if(target.type() != Type.WHITEBOARD)
			return Result.FAILURE;
		WhiteboardRef dest = ((WhiteboardValue)target).assignment();
		if(dest == null)
			return Result.FAILURE;
		
		IWhiteboardObject<?> result = getResult(tricksy, local, global, parent);
		if(result == null || result.isEmpty() || result.size() == 0)
			return Result.FAILURE;
		
		local.setValue(dest, result);
		return Result.SUCCESS;
	}
	
	public abstract void addInputVariables(Map<WhiteboardRef, INodeInput> set);
	
	@Nullable
	public abstract <N extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<?> getResult(N tricksy, LocalWhiteboard<N> local, GlobalWhiteboard global, LeafNode parent);
	
	/** Returns a provided Region or generates one around a provided position */
	@Nullable
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
		
		BlockPos point = null;
		if(pos.size() == 0)
			point = fallback.apply(tricksy);
		else
			point = pos.as(TFObjType.BLOCK).get();
		
		return point == null ? null : new RegionSphere(point, range.get());
	}
}
