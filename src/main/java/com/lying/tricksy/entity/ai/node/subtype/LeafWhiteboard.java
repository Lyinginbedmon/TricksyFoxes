package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.api.entity.ai.INodeIO;
import com.lying.tricksy.api.entity.ai.INodeIOValue;
import com.lying.tricksy.api.entity.ai.INodeIOValue.Type;
import com.lying.tricksy.api.entity.ai.INodeIOValue.WhiteboardValue;
import com.lying.tricksy.api.entity.ai.INodeTickHandler;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.node.handler.NodeInput;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjEntity;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.init.TFSoundEvents;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class LeafWhiteboard implements ISubtypeGroup<LeafNode>
{
	public static final Identifier VARIANT_CYCLE = ISubtypeGroup.variant("cycle_value");
	public static final Identifier VARIANT_SORT_NEAREST = ISubtypeGroup.variant("sort_nearest");
	public static final Identifier VARIANT_SORT_CONTIGUOUS = ISubtypeGroup.variant("sort_contiguous");
	public static final Identifier VARIANT_SORT_COLUMNAR = ISubtypeGroup.variant("sort_columnar");
	public static final Identifier VARIANT_SORT_VERTICAL = ISubtypeGroup.variant("sort_vertical");
	public static final Identifier VARIANT_COPY = ISubtypeGroup.variant("set_value");
	public static final Identifier VARIANT_CLEAR = ISubtypeGroup.variant("clear_value");
	
	public Identifier getRegistryName() { return new Identifier(Reference.ModInfo.MOD_ID, "leaf_whiteboard"); }
	
	public Collection<NodeSubType<LeafNode>> getSubtypes()
	{
		List<NodeSubType<LeafNode>> set = Lists.newArrayList();
		add(set, VARIANT_CYCLE, new INodeTickHandler<LeafNode>()
		{
			public static final WhiteboardRef VAR_A = new WhiteboardRef("value_to_cycle", TFObjType.BOOL).displayName(CommonVariables.translate("to_cycle"));
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(VAR_A, NodeInput.makeInput(NodeInput.anyLocal()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<?> value = getOrDefault(VAR_A, parent, local, global);
				if(!value.isList())
					return Result.FAILURE;
				
				value.cycle();
				return Result.SUCCESS;
			}
		});
		add(set, VARIANT_SORT_NEAREST, leafSortNearest());
		add(set, VARIANT_SORT_COLUMNAR, leafSortColumnar());
		add(set, VARIANT_SORT_CONTIGUOUS, leafSortContiguous());
		add(set, VARIANT_SORT_VERTICAL, leafSortVertical());
		add(set, VARIANT_COPY, new INodeTickHandler<LeafNode>()
		{
			public static final WhiteboardRef COPY = new WhiteboardRef("value_to_copy", TFObjType.BOOL).displayName(CommonVariables.translate("to_copy"));
			public static final WhiteboardRef DEST = new WhiteboardRef("target_reference", TFObjType.BOOL).displayName(CommonVariables.translate("ref_target"));
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(
						COPY, NodeInput.makeInput(NodeInput.any(), TFObjType.EMPTY.blank(), Text.literal("")),
						DEST, NodeInput.makeInput((var) -> !var.uncached() && !var.boardType().isReadOnly()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<?> value = getOrDefault(COPY, parent, local, global);
				INodeIOValue targetVal = parent.getIO(DEST);
				WhiteboardRef target = targetVal.type() == Type.WHITEBOARD ? ((WhiteboardValue)targetVal).assignment() : null;
				if(target == null)
					return Result.FAILURE;
				
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
		add(set, VARIANT_CLEAR, new INodeTickHandler<LeafNode>()
		{
			public static final WhiteboardRef DEST = new WhiteboardRef("target_reference", TFObjType.BOOL).displayName(CommonVariables.translate("ref_target"));
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(
						DEST, NodeInput.makeInput((var) -> !var.uncached() && !var.boardType().isReadOnly()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				INodeIOValue targetVal = parent.getIO(DEST);
				if(targetVal.type() != Type.WHITEBOARD)
					return Result.FAILURE;
				
				WhiteboardRef target = ((WhiteboardValue)targetVal).assignment();
				if(target == null)
					return Result.FAILURE;
				
				local.setValue(target, target.type().blank());
				
				tricksy.getWorld().playSound(null, tricksy.getBlockPos(), TFSoundEvents.WHITEBOARD_UPDATED, SoundCategory.NEUTRAL, 1F, 0.75F + tricksy.getRandom().nextFloat());
				return Result.SUCCESS;
			}
		});
		return set;
	}
	
	public static INodeTickHandler<LeafNode> leafSortNearest()
	{
		return new SortHandler()
		{
			private static boolean isInverted;
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(
						VAR_UNSORTED, UNSORTED_INPUT,
						POSITION, NodeInput.makeInput(NodeInput.ofType(TFObjType.BLOCK, false), new WhiteboardObjBlock(), LocalWhiteboard.SELF.displayName()),
						INVERT, NodeInput.makeInput(NodeInput.ofType(TFObjType.BOOL, true), new WhiteboardObj.Bool(), (new WhiteboardObj.Bool(false)).describe().get(0)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				INodeIOValue reference = parent.getIO(VAR_UNSORTED);
				if(reference.type() != Type.WHITEBOARD)
					return Result.FAILURE;
				
				IWhiteboardObject<?> value = getOrDefault(VAR_UNSORTED, parent, local, global);
				if(value.isEmpty())
					return Result.FAILURE;
				
				IWhiteboardObject<?> pos = getOrDefault(CommonVariables.VAR_POS, parent, local, global);
				Vec3d origin = (pos.size() == 0 ? tricksy.getBlockPos() : pos.as(TFObjType.BLOCK).get()).toCenterPos();
				
				isInverted = getOrDefault(INVERT, parent, local, global).as(TFObjType.BOOL).get();
				
				IWhiteboardObject<?> sorted;
				if(!value.isList() || value.size() < 2)
					sorted = value.copy();
				else if(value.type() == TFObjType.BLOCK)
				{
					WhiteboardObjBlock result = new WhiteboardObjBlock();
					sort(value.as(TFObjType.BLOCK).getAll(), origin, ent -> ent.toCenterPos()).forEach(ent -> result.add(ent));
					sorted = result;
				}
				else if(value.type() == TFObjType.ENT)
				{
					WhiteboardObjEntity result = new WhiteboardObjEntity();
					sort(value.as(TFObjType.ENT).getAll(), origin, ent -> ent.getPos()).forEach(ent -> result.add(ent));
					sorted = result;
				}
				else
					return Result.FAILURE;
				
				local.setValue(((WhiteboardValue)reference).assignment(), sorted);
				tricksy.getWorld().playSound(null, tricksy.getBlockPos(), TFSoundEvents.WHITEBOARD_UPDATED, SoundCategory.NEUTRAL, 1F, 0.75F + tricksy.getRandom().nextFloat());
				return Result.SUCCESS;
			}
			
			public <T extends Object> List<T> sort(final List<T> set, Vec3d origin, Function<T, Vec3d> converter)
			{
				List<T> sortedList = Lists.newArrayList();
				sortedList.addAll(set);
				
				sortedList.sort((o1,o2) -> 
				{
					double dist1 = converter.apply(o1).distanceTo(origin) * (isInverted ? -1 : 1);
					double dist2 = converter.apply(o2).distanceTo(origin) * (isInverted ? -1 : 1);
					return dist1 < dist2 ? -1 : dist1 > dist2 ? 1 : 0;
				});
				
				return sortedList;
			}
		};
	}
	
	public static INodeTickHandler<LeafNode> leafSortColumnar()
	{
		return new SortHandler()
		{
			/** Sorts the input points based on their containment within a cube expanding from the origin point */
			public <T extends Object> List<T> sort(final List<T> set, Vec3d origin, Function<T, Vec3d> converter)
			{
				List<T> sortedList = Lists.newArrayList();
				
				// Collect points into layers of a nested cube
				Map<Integer, List<T>> pointMap = new HashMap<>();
				for(T point : set)
				{
					Vec3d offset = converter.apply(point).subtract(origin);
					int dist = Math.max(Math.max((int)Math.abs(offset.x), (int)Math.abs(offset.y)), (int)Math.abs(offset.z));
					List<T> group = pointMap.getOrDefault(dist, Lists.newArrayList());
					group.add(point);
					pointMap.put(dist, group);
				}
				
				// XXX This feels like overcomplicating things a bit...
				
				// Collect points in each layer into ascending columns
				Vec3i originLat = new Vec3i((int)origin.x, 0, (int)origin.z);
				Comparator<T> sorter = new Comparator<T>()
						{
							public int compare(T o1, T o2)
							{
								Vec3d vec1 = converter.apply(o1);
								Vec3d vec2 = converter.apply(o2);
								
								Vec3i lat1 = new Vec3i((int)vec1.x, 0, (int)vec1.z);
								Vec3i lat2 = new Vec3i((int)vec2.x, 0, (int)vec2.z);
								if(lat1.getX() ==  lat2.getX() && lat1.getZ() == lat2.getZ())
								{
									double off1 = Math.abs(origin.y - vec1.y);
									double off2 = Math.abs(origin.y - vec2.y);
									return off1 < off2 ? -1 : off1 > off2 ? 1 : 0;
								}
								
								double lat1L = lat1.getSquaredDistance(originLat);
								double lat2L = lat2.getSquaredDistance(originLat);
								return lat1L < lat2L ? -1 : lat1L > lat2L ? 1 : 0;
							}
						};
				
				pointMap.keySet().stream().forEach(index -> 
				{
					List<T> points = pointMap.get(index);
					points.sort(sorter);
					points.stream().forEach(point -> sortedList.add(point));
				});
				
				return sortedList;
			}
		};
	}
	
	public static INodeTickHandler<LeafNode> leafSortContiguous()
	{
		return new SortHandler()
			{
				public <T extends Object> List<T> sort(final List<T> set, Vec3d origin, Function<T, Vec3d> converter)
				{
					List<T> sortedList = Lists.newArrayList();
					
					List<T> values = Lists.newArrayList();
					values.addAll(set);
					
					values.sort((o1,o2) -> 
					{
						double dist1 = converter.apply(o1).distanceTo(origin);
						double dist2 = converter.apply(o2).distanceTo(origin);
						return dist1 < dist2 ? -1 : dist1 > dist2 ? 1 : 0;
					});
					
					T initialFocus = values.remove(0);
					Vec3d focalPoint = converter.apply(initialFocus);
					sortedList.add(initialFocus);
					
					while(!values.isEmpty())
					{
						if(values.size() == 1)
						{
							sortedList.add(values.get(0));
							break;
						}
						
						// Sort points by distance to the current focal point
						values.sort(distToVec(focalPoint, converter));
						double minDist = converter.apply(values.get(0)).distanceTo(focalPoint);
						
						// Filter out all points with the same distance to the focal point
						List<T> allSameDist = Lists.newArrayList();
						values.stream().filter(sameDistTo(focalPoint, converter, minDist)).forEach(val -> allSameDist.add(val));
						values.removeAll(allSameDist);
						
						// Add points to the sorted list based on their distance to the origin point
						allSameDist.sort(distToVec(origin, converter));
						Vec3d nextFocal = converter.apply(allSameDist.get(0));
						sortedList.addAll(allSameDist);
						
						focalPoint = nextFocal;
					}
					
					return sortedList;
				}
				
				private static <T extends Object> Predicate<T> sameDistTo(Vec3d currentVec, Function<T, Vec3d> converter, double dist)
				{
					return val -> converter.apply(val).distanceTo(currentVec) == dist;
				}
				
				private static <T extends Object> Comparator<T> distToVec(Vec3d currentVec, Function<T, Vec3d> converter)
				{
					return (o1,o2) -> 
					{
						double dist1 = converter.apply(o1).distanceTo(currentVec);
						double dist2 = converter.apply(o2).distanceTo(currentVec);
						return dist1 < dist2 ? -1 : dist1 > dist2 ? 1 : 0;
					};
				}
			};
	}
	
	public static INodeTickHandler<LeafNode> leafSortVertical()
	{
		return new SortHandler()
		{
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(
						VAR_UNSORTED, UNSORTED_INPUT,
						INVERT, NodeInput.makeInput(NodeInput.ofType(TFObjType.BOOL, true), new WhiteboardObj.Bool(), (new WhiteboardObj.Bool(false)).describe().get(0)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				INodeIOValue reference = parent.getIO(VAR_UNSORTED);
				if(reference.type() != Type.WHITEBOARD)
					return Result.FAILURE;
				
				IWhiteboardObject<?> value = getOrDefault(VAR_UNSORTED, parent, local, global);
				if(value.isEmpty())
					return Result.FAILURE;
				
				boolean inverted = getOrDefault(INVERT, parent, local, global).as(TFObjType.BOOL).get();
				Vec3d origin = inverted ? new Vec3d(0, -1, 0) : new Vec3d(0, 1, 0);
				
				IWhiteboardObject<?> sorted;
				if(!value.isList() || value.size() < 2)
					sorted = value.copy();
				else if(value.type() == TFObjType.BLOCK)
				{
					WhiteboardObjBlock result = new WhiteboardObjBlock();
					sort(value.as(TFObjType.BLOCK).getAll(), origin, ent -> ent.toCenterPos()).forEach(ent -> result.add(ent));
					sorted = result;
				}
				else if(value.type() == TFObjType.ENT)
				{
					WhiteboardObjEntity result = new WhiteboardObjEntity();
					sort(value.as(TFObjType.ENT).getAll(), origin, ent -> ent.getPos()).forEach(ent -> result.add(ent));
					sorted = result;
				}
				else
					return Result.FAILURE;
				
				local.setValue(((WhiteboardValue)reference).assignment(), sorted);
				tricksy.getWorld().playSound(null, tricksy.getBlockPos(), TFSoundEvents.WHITEBOARD_UPDATED, SoundCategory.NEUTRAL, 1F, 0.75F + tricksy.getRandom().nextFloat());
				return Result.SUCCESS;
			}
			
			public <T extends Object> List<T> sort(final List<T> set, Vec3d origin, Function<T, Vec3d> converter)
			{
				List<T> sortedList = Lists.newArrayList();
				sortedList.addAll(set);
				
				sortedList.sort((o1,o2) -> 
				{
					Vec3d vec1 = converter.apply(o1);
					Vec3d vec2 = converter.apply(o2);
					if(vec1.distanceTo(vec2) == 0D)
						return 0;
					
					double[] val1 = new double[] {vec1.y * origin.y, vec1.x, vec1.z};
					double[] val2 = new double[] {vec2.y * origin.y, vec2.x, vec2.z};
					for(int i=0; i<val1.length; i++)
						if(val1[i] != val2[i])
							return val1[i] < val2[i] ? -1 : val1[i] > val2[i] ? 1 : 0;
					
					return 0;
				});
				
				return sortedList;
			}
		};
	}
	
	private static interface SortHandler extends INodeTickHandler<LeafNode>
	{
		public static final WhiteboardRef VAR_UNSORTED = new WhiteboardRef("value_to_cycle", TFObjType.BLOCK).displayName(CommonVariables.translate("to_cycle"));
		public static final NodeInput UNSORTED_INPUT = new NodeInput()
		{
			public Predicate<WhiteboardRef> predicate() { return (ref) -> (ref.type() == TFObjType.BLOCK || ref.type() == TFObjType.ENT) && !ref.boardType().isReadOnly(); }
			
			public boolean allowStatic() { return false; }
		};
		public static final WhiteboardRef POSITION = CommonVariables.VAR_POS;
		public static final WhiteboardRef INVERT = new WhiteboardRef("invert", TFObjType.BOOL).displayName(CommonVariables.translate("invert"));
		
		public default <T extends PathAwareEntity & ITricksyMob<?>> int getCooldown(T tricksy) { return 20; }
		
		public default Map<WhiteboardRef, INodeIO> ioSet()
		{
			return Map.of(
					VAR_UNSORTED, UNSORTED_INPUT,
					POSITION, NodeInput.makeInput(NodeInput.ofType(TFObjType.BLOCK, false), new WhiteboardObjBlock(), LocalWhiteboard.SELF.displayName()));
		}
		
		public default <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
		{
			INodeIOValue reference = parent.getIO(VAR_UNSORTED);
			if(reference.type() != Type.WHITEBOARD)
				return Result.FAILURE;
			
			IWhiteboardObject<?> value = getOrDefault(VAR_UNSORTED, parent, local, global);
			if(value.isEmpty())
				return Result.FAILURE;
			
			IWhiteboardObject<?> pos = getOrDefault(CommonVariables.VAR_POS, parent, local, global);
			Vec3d origin = (pos.size() == 0 ? tricksy.getBlockPos() : pos.as(TFObjType.BLOCK).get()).toCenterPos();
			
			IWhiteboardObject<?> sorted;
			if(!value.isList() || value.size() < 2)
				sorted = value.copy();
			else if(value.type() == TFObjType.BLOCK)
			{
				WhiteboardObjBlock result = new WhiteboardObjBlock();
				sort(value.as(TFObjType.BLOCK).getAll(), origin, ent -> ent.toCenterPos()).forEach(ent -> result.add(ent));
				sorted = result;
			}
			else if(value.type() == TFObjType.ENT)
			{
				WhiteboardObjEntity result = new WhiteboardObjEntity();
				sort(value.as(TFObjType.ENT).getAll(), origin, ent -> ent.getPos()).forEach(ent -> result.add(ent));
				sorted = result;
			}
			else
				return Result.FAILURE;
			
			local.setValue(((WhiteboardValue)reference).assignment(), sorted);
			tricksy.getWorld().playSound(null, tricksy.getBlockPos(), TFSoundEvents.WHITEBOARD_UPDATED, SoundCategory.NEUTRAL, 1F, 0.75F + tricksy.getRandom().nextFloat());
			return Result.SUCCESS;
		}
		
		public <T extends Object> List<T> sort(List<T> set, Vec3d origin, Function<T, Vec3d> converter);
	}
}
