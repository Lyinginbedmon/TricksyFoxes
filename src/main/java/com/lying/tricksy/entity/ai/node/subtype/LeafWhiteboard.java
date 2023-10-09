package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.node.handler.INodeInput;
import com.lying.tricksy.entity.ai.node.handler.NodeTickHandler;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.BoardType;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Global;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Local;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.init.TFSoundEvents;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class LeafWhiteboard implements ISubtypeGroup<LeafNode>
{
	public static final Identifier VARIANT_CYCLE = ISubtypeGroup.variant("cycle_value");
	public static final Identifier VARIANT_SORT_NEAREST = ISubtypeGroup.variant("sort_nearest");
	public static final Identifier VARIANT_COPY = ISubtypeGroup.variant("set_value");
	
	public void addActions(Collection<NodeSubType<LeafNode>> set)
	{
		add(set, VARIANT_CYCLE, new NodeTickHandler<LeafNode>()
		{
			public static final WhiteboardRef VAR_A = new WhiteboardRef("value_to_cycle", TFObjType.BOOL).displayName(CommonVariables.translate("to_cycle"));
			
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(VAR_A, INodeInput.makeInput(NodeTickHandler.anyLocal()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				IWhiteboardObject<?> value = getOrDefault(VAR_A, parent, local, global);
				if(!value.isList())
					return Result.FAILURE;
				
				value.cycle();
				return Result.SUCCESS;
			}
		});
		add(set, VARIANT_SORT_NEAREST, new NodeTickHandler<LeafNode>()
		{
			public static final WhiteboardRef VAR_A = new WhiteboardRef("value_to_cycle", TFObjType.BLOCK).displayName(CommonVariables.translate("to_cycle"));
			private static BlockPos position;
			
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(
						VAR_A, INodeInput.makeInput((ref) -> ref.type().castableTo(TFObjType.BLOCK) && ref.boardType() == BoardType.LOCAL),
						CommonVariables.VAR_POS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK), new WhiteboardObjBlock(), Whiteboard.Local.SELF.displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				WhiteboardRef reference = parent.variable(VAR_A);
				IWhiteboardObject<BlockPos> value = getOrDefault(VAR_A, parent, local, global).as(TFObjType.BLOCK);
				IWhiteboardObject<?> pos = getOrDefault(CommonVariables.VAR_POS, parent, local, global);
				
				position = null;
				if(pos.size() == 0)
					position = tricksy.getBlockPos();
				else
					position = pos.as(TFObjType.BLOCK).get();
				
				if(value.isEmpty() || !value.isList())
					return Result.FAILURE;
				
				List<BlockPos> points = value.getAll();
				points.sort(new Comparator<BlockPos>() 
				{
					public int compare(BlockPos o1, BlockPos o2)
					{
						double dist1 = o1.getSquaredDistance(position);
						double dist2 = o2.getSquaredDistance(position);
						return dist1 < dist2 ? -1 : dist1 > dist2 ? 1 : 0;
					}
				});
				
				tricksy.logStatus(Text.literal("Closest position was "+points.get(0).toShortString()));
				WhiteboardObjBlock sorted = new WhiteboardObjBlock();
				points.forEach((point) -> sorted.add(point));
				local.setValue(reference, sorted);
				tricksy.getWorld().playSound(null, tricksy.getBlockPos(), TFSoundEvents.WHITEBOARD_UPDATED, SoundCategory.NEUTRAL, 1F, 0.75F + tricksy.getRandom().nextFloat());
				return Result.SUCCESS;
			}
		});
		add(set, VARIANT_COPY, new NodeTickHandler<LeafNode>()
		{
			public static final WhiteboardRef COPY = new WhiteboardRef("value_to_copy", TFObjType.BOOL).displayName(CommonVariables.translate("to_copy"));
			public static final WhiteboardRef DEST = new WhiteboardRef("target_reference", TFObjType.BOOL).displayName(CommonVariables.translate("ref_target"));
			
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(
						COPY, INodeInput.makeInput(NodeTickHandler.any(), TFObjType.EMPTY.blank(), Text.literal("")),
						DEST, INodeInput.makeInput((var) -> !var.uncached() && var.boardType() == BoardType.LOCAL));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				IWhiteboardObject<?> value = getOrDefault(COPY, parent, local, global);
				WhiteboardRef target = parent.variable(DEST);
				
				if(value.type() != TFObjType.EMPTY)
				{
					if(!target.type().castableTo(value.type()))
						return Result.FAILURE;
					
					local.setValue(target, value.as(target.type()));
				}
				else
					local.setValue(target, target.type().blank());
				
				tricksy.getWorld().playSound(null, tricksy.getBlockPos(), TFSoundEvents.WHITEBOARD_UPDATED, SoundCategory.NEUTRAL, 1F, 0.75F + tricksy.getRandom().nextFloat());
				return Result.SUCCESS;
			}
		});
	}
}
