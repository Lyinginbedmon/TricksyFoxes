package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.api.entity.ai.INodeIO;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.handler.GetterHandlerTyped;
import com.lying.tricksy.entity.ai.node.handler.GetterHandlerUntyped;
import com.lying.tricksy.entity.ai.node.handler.NodeInput;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.ConstantsWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardManager;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.init.TFNodeStatus;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

public class LeafArithmetic extends NodeGroupLeaf
{
	public static NodeSubType<LeafNode> ADD;
	public static NodeSubType<LeafNode> MUL;
	public static NodeSubType<LeafNode> MOD;
	public static NodeSubType<LeafNode> GET_COORD;
	public static NodeSubType<LeafNode> COMPOSE;
	public static NodeSubType<LeafNode> OFFSET;
	public static NodeSubType<LeafNode> RANDOM;
	public static NodeSubType<LeafNode> RANDOM_POS;
	
	public Identifier getRegistryName() { return new Identifier(Reference.ModInfo.MOD_ID, "leaf_arithmetic"); }
	
	public Collection<NodeSubType<LeafNode>> getSubtypes()
	{
		List<NodeSubType<LeafNode>> set = Lists.newArrayList();
		set.add(ADD = subtype(ISubtypeGroup.variant("addition"), new GetterHandlerUntyped(TFObjType.INT, TFObjType.BLOCK)
		{
			public void addInputVariables(Map<WhiteboardRef, INodeIO> set)
			{
				set.put(CommonVariables.VAR_A, NUM_OR_POS);
				set.put(CommonVariables.VAR_B, NodeInput.makeInput(ref -> ref.type().castableTo(TFObjType.INT) || ref.type().castableTo(TFObjType.BLOCK)));
				set.put(CommonVariables.SUBTRACT, NodeInput.makeInput(NodeInput.ofType(TFObjType.BOOL, false), new WhiteboardObj.Bool(), Text.translatable("value."+Reference.ModInfo.MOD_ID+".boolean.false")));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<?> getResult(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				IWhiteboardObject<?> intA = getOrDefault(CommonVariables.VAR_A, parent, whiteboards);
				IWhiteboardObject<?> intB = getOrDefault(CommonVariables.VAR_B, parent, whiteboards);
				int mul = getOrDefault(CommonVariables.SUBTRACT, parent, whiteboards).as(TFObjType.BOOL).get() ? -1 : 1;
				
				// Add an integer to all coordinates of a position
				if(intA.type() != intB.type())
				{
					int num = (intA.type() == TFObjType.INT ? intA.as(TFObjType.INT).get() : intB.as(TFObjType.INT).get()) * mul;
					IWhiteboardObject<BlockPos> pos = intA.type() == TFObjType.BLOCK ? intA.as(TFObjType.BLOCK) : intB.as(TFObjType.BLOCK);
					return new WhiteboardObjBlock(pos.get().add(num, num, num), ((WhiteboardObjBlock)pos).direction());
				}
				// Add two integers
				else if(intA.type() == TFObjType.INT)
					return new WhiteboardObj.Int(intA.as(TFObjType.INT).get() + (intB.as(TFObjType.INT).get() * mul));
				// Add two positions
				else if(intA.type() == TFObjType.BLOCK)
					return new WhiteboardObjBlock(intA.as(TFObjType.BLOCK).get().add(intB.as(TFObjType.BLOCK).get().multiply(mul)));
				
				parent.logStatus(TFNodeStatus.INPUT_ERROR);
				return null;
			}
		}));
		set.add(MUL = subtype(ISubtypeGroup.variant("multiplication"), new GetterHandlerUntyped(TFObjType.INT, TFObjType.BLOCK)
		{
			private static final WhiteboardRef DIV = new WhiteboardRef("divide", TFObjType.BOOL).displayName(CommonVariables.translate("divide"));
			
			public void addInputVariables(Map<WhiteboardRef, INodeIO> set)
			{
				set.put(CommonVariables.VAR_A, NUM_OR_POS);
				set.put(CommonVariables.VAR_B, NodeInput.makeInput(ref -> ref.type().castableTo(TFObjType.INT) || ref.type().castableTo(TFObjType.BLOCK)));
				set.put(DIV, NodeInput.makeInput(NodeInput.ofType(TFObjType.BOOL, false), new WhiteboardObj.Bool(), Text.translatable("value."+Reference.ModInfo.MOD_ID+".boolean.false")));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<?> getResult(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				IWhiteboardObject<?> intA = getOrDefault(CommonVariables.VAR_A, parent, whiteboards);
				IWhiteboardObject<?> intB = getOrDefault(CommonVariables.VAR_B, parent, whiteboards);
				boolean isDivision = getOrDefault(DIV, parent, whiteboards).as(TFObjType.BOOL).get();
				
				// Multiply a position by an integer
				if(intA.type() != intB.type())
				{
					IWhiteboardObject<BlockPos> pos = intA.type() == TFObjType.BLOCK ? intA.as(TFObjType.BLOCK) : intB.as(TFObjType.BLOCK);
					int num = (intA.type() == TFObjType.INT ? intA.as(TFObjType.INT).get() : intB.as(TFObjType.INT).get());
					if(isDivision)
						num = 1 / num;
					return new WhiteboardObjBlock(pos.get().multiply(num), ((WhiteboardObjBlock)pos).direction());
				}
				// Multiply two integers
				else if(intA.type() == TFObjType.INT)
				{
					int num = intB.as(TFObjType.INT).get();
					if(isDivision)
						num = 1 / num;
					return new WhiteboardObj.Int(intA.as(TFObjType.INT).get() * num);
				}
				// Multiply two positions
				else if(intA.type() == TFObjType.BLOCK)
				{
					IWhiteboardObject<BlockPos> pos = intA.as(TFObjType.BLOCK);
					BlockPos num = intB.as(TFObjType.BLOCK).get();
					if(isDivision)
						num = new BlockPos(1 / num.getX(), 1 / num.getY(), 1 / num.getZ());
					return new WhiteboardObjBlock(new BlockPos(pos.get().getX() * num.getX(), pos.get().getY() * num.getY(), pos.get().getZ() * num.getZ()), ((WhiteboardObjBlock)pos).direction());
				}
				
				return null;
			}
		}));
		set.add(MOD = subtype(ISubtypeGroup.variant("modulus"), new GetterHandlerTyped<Integer>(TFObjType.INT)
		{
			public void addInputVariables(Map<WhiteboardRef, INodeIO> set)
			{
				set.put(CommonVariables.VAR_A, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true)));
				set.put(CommonVariables.VAR_NUM, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int(4), Text.literal("4")));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Integer> getTypedResult(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				int intA = getOrDefault(CommonVariables.VAR_A, parent, whiteboards).as(TFObjType.INT).get();
				int intB = getOrDefault(CommonVariables.VAR_B, parent, whiteboards).as(TFObjType.INT).get();
				return new WhiteboardObj.Int(intA % intB);
			}
		}));
		set.add(GET_COORD = subtype(ISubtypeGroup.variant("get_coordinate"), new GetterHandlerTyped<Integer>(TFObjType.INT)
		{
			public void addInputVariables(Map<WhiteboardRef, INodeIO> set)
			{
				set.put(CommonVariables.VAR_POS, NodeInput.makeInput(NodeInput.ofType(TFObjType.BLOCK, true)));
				set.put(CommonVariables.VAR_NUM, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int(0), Text.literal("0")));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Integer> getTypedResult(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				BlockPos pos = getOrDefault(CommonVariables.VAR_POS, parent, whiteboards).as(TFObjType.BLOCK).get();
				int ind = Math.abs(getOrDefault(CommonVariables.VAR_NUM, parent, whiteboards).as(TFObjType.INT).get()) % 3;
				return new WhiteboardObj.Int(new int[] {pos.getX(), pos.getY(), pos.getZ()}[ind]);
			}
		}));
		set.add(COMPOSE = subtype(ISubtypeGroup.variant("compose_position"), new GetterHandlerTyped<BlockPos>(TFObjType.BLOCK)
		{
			public void addInputVariables(Map<WhiteboardRef, INodeIO> set)
			{
				set.put(CommonVariables.X, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int(0), Text.literal("0")));
				set.put(CommonVariables.Y, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int(0), Text.literal("0")));
				set.put(CommonVariables.Z, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int(0), Text.literal("0")));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<BlockPos> getTypedResult(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				int x = getOrDefault(CommonVariables.X, parent, whiteboards).as(TFObjType.INT).get();
				int y = getOrDefault(CommonVariables.Y, parent, whiteboards).as(TFObjType.INT).get();
				int z = getOrDefault(CommonVariables.Z, parent, whiteboards).as(TFObjType.INT).get();
				return new WhiteboardObjBlock(new BlockPos(x, y, z));
			}
		}));
		set.add(OFFSET = subtype(ISubtypeGroup.variant("offset"), new GetterHandlerTyped<BlockPos>(TFObjType.BLOCK)
		{
			public void addInputVariables(Map<WhiteboardRef, INodeIO> set)
			{
				set.put(CommonVariables.VAR_A, NodeInput.makeInput(NodeInput.ofType(TFObjType.BLOCK, false)));
				set.put(CommonVariables.VAR_B, NodeInput.makeInput(NodeInput.ofType(TFObjType.BLOCK, true), new WhiteboardObjBlock(BlockPos.ORIGIN, Direction.NORTH), ConstantsWhiteboard.DIRECTIONS.get(Direction.NORTH).displayName()));
				set.put(CommonVariables.VAR_NUM, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int(1), Text.literal("1")));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<BlockPos> getTypedResult(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				IWhiteboardObject<BlockPos> intA = getOrDefault(CommonVariables.VAR_A, parent, whiteboards).as(TFObjType.BLOCK);
				IWhiteboardObject<BlockPos> intB = getOrDefault(CommonVariables.VAR_B, parent, whiteboards).as(TFObjType.BLOCK);
				int num = getOrDefault(CommonVariables.VAR_NUM, parent, whiteboards).as(TFObjType.INT).get();
				
				BlockPos pos = intA.get();
				Direction face = ((WhiteboardObjBlock)intB).direction();
				return new WhiteboardObjBlock(pos.offset(face, num), ((WhiteboardObjBlock)intA).direction());
			}
		}));
		set.add(RANDOM = subtype(ISubtypeGroup.variant("random"), new GetterHandlerTyped<Integer>(TFObjType.INT)
		{
			public void addInputVariables(Map<WhiteboardRef, INodeIO> set)
			{
				set.put(CommonVariables.VAR_NUM, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int(3), Text.literal("3")));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Integer> getTypedResult(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				int range = Math.max(1, getOrDefault(CommonVariables.VAR_NUM, parent, whiteboards).as(TFObjType.INT).get());
				return new WhiteboardObj.Int(tricksy.getRandom().nextInt(range));
			}
		}));
		set.add(RANDOM_POS = subtype(ISubtypeGroup.variant("random_position"), new GetterHandlerTyped<BlockPos>(TFObjType.BLOCK) 
		{
			public void addInputVariables(Map<WhiteboardRef, INodeIO> set)
			{
				set.put(CommonVariables.X, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int(3), Text.literal("3")));
				set.put(CommonVariables.Y, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int(3), Text.literal("3")));
				set.put(CommonVariables.Z, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int(3), Text.literal("3")));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<BlockPos> getTypedResult(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				int x = getOrDefault(CommonVariables.X, parent, whiteboards).as(TFObjType.INT).get();
				int y = getOrDefault(CommonVariables.Y, parent, whiteboards).as(TFObjType.INT).get();
				int z = getOrDefault(CommonVariables.Z, parent, whiteboards).as(TFObjType.INT).get();
				
				Random rand = tricksy.getRandom();
				x = x < 1 ? 0 : rand.nextBetween(-x, x);
				y = y < 1 ? 0 : rand.nextBetween(-y, y);
				z = z < 1 ? 0 : rand.nextBetween(-z, z);
				BlockPos pos = new BlockPos(x, y, z);
				
				return new WhiteboardObjBlock(pos);
			}
		}));
		return set;
	}
}