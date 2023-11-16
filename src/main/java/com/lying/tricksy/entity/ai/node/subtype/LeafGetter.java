package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.handler.GetterHandler;
import com.lying.tricksy.entity.ai.node.handler.INodeInput;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.ConstantsWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjEntity;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class LeafGetter implements ISubtypeGroup<LeafNode>
{
	public static final Identifier VARIANT_GET_DISTANCE = ISubtypeGroup.variant("distance_to");
	
	// Entity value getters
	public static final Identifier VARIANT_GET_ASSAILANT = ISubtypeGroup.variant("get_assailant");
	public static final Identifier VARIANT_GET_HEALTH = ISubtypeGroup.variant("get_health");
	public static final Identifier VARIANT_GET_HELD = ISubtypeGroup.variant("get_held_item");
	public static final Identifier VARIANT_GET_BARK = ISubtypeGroup.variant("get_bark");
	
	// Whiteboard getters
	public static final Identifier VARIANT_ADD = ISubtypeGroup.variant("addition");
	public static final Identifier VARIANT_OFFSET = ISubtypeGroup.variant("offset");
	
	public Identifier getRegistryName() { return new Identifier(Reference.ModInfo.MOD_ID, "leaf_getter"); }
	
	public Collection<NodeSubType<LeafNode>> getSubtypes()
	{
		List<NodeSubType<LeafNode>> set = Lists.newArrayList();
		add(set, VARIANT_GET_ASSAILANT, new GetterHandler<Entity>(TFObjType.ENT)
		{
			public void addInputVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.TARGET_ENT, INodeInput.makeInput(INodeInput.ofType(TFObjType.ENT, false), new WhiteboardObjEntity(), LocalWhiteboard.SELF.displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Entity> getResult(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<Entity> target = getOrDefault(CommonVariables.TARGET_ENT, parent, local, global).as(TFObjType.ENT);
				
				Entity entity = target.size() == 0 ? tricksy : target.get();
				if(!(entity instanceof LivingEntity))
					return null;
				
				Entity assailant = ((LivingEntity)entity).getAttacker();
				return assailant == null || assailant == tricksy ? new WhiteboardObjEntity() : new WhiteboardObjEntity(assailant);
			}
		});
		add(set, VARIANT_GET_HEALTH, new GetterHandler<Integer>(TFObjType.INT)
		{
			private static final WhiteboardRef MAX = new WhiteboardRef("max_health", TFObjType.BOOL).displayName(CommonVariables.translate("max_health"));
			
			public void addInputVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.TARGET_ENT, INodeInput.makeInput(INodeInput.ofType(TFObjType.ENT, false), new WhiteboardObjEntity(), LocalWhiteboard.SELF.displayName()));
				set.put(MAX, INodeInput.makeInput(INodeInput.ofType(TFObjType.BOOL, false), new WhiteboardObj.Bool(false)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Integer> getResult(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<Entity> target = getOrDefault(CommonVariables.TARGET_ENT, parent, local, global).as(TFObjType.ENT);
				IWhiteboardObject<Boolean> isMax = getOrDefault(MAX, parent, local, global).as(TFObjType.BOOL);
				
				Entity entity = target.size() == 0 ? tricksy : target.get();
				if(!(entity instanceof LivingEntity) || !entity.isAlive())
					return null;
				
				LivingEntity living = (LivingEntity)entity;
				return new WhiteboardObj.Int((int)(isMax.get() ? living.getMaxHealth() : living.getHealth()));
			}
		});
		add(set, VARIANT_GET_HELD, new GetterHandler<ItemStack>(TFObjType.ITEM)
		{
			private static final WhiteboardRef OFF = new WhiteboardRef("offhand", TFObjType.BOOL).displayName(CommonVariables.translate("offhand"));
			
			public void addInputVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.TARGET_ENT, INodeInput.makeInput(INodeInput.ofType(TFObjType.ENT, false), new WhiteboardObjEntity(), LocalWhiteboard.SELF.displayName()));
				set.put(OFF, INodeInput.makeInput(INodeInput.ofType(TFObjType.BOOL, false), new WhiteboardObj.Bool(false)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<ItemStack> getResult(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<Entity> target = getOrDefault(CommonVariables.TARGET_ENT, parent, local, global).as(TFObjType.ENT);
				IWhiteboardObject<Boolean> isOff = getOrDefault(OFF, parent, local, global).as(TFObjType.BOOL);
				
				Entity entity = target.size() == 0 ? tricksy : target.get();
				if(!(entity instanceof LivingEntity))
					return null;
				
				LivingEntity living = (LivingEntity)entity;
				return new WhiteboardObj.Item(isOff.get() ? living.getOffHandStack() : living.getMainHandStack());
			}
		});
		add(set, VARIANT_GET_DISTANCE, new GetterHandler<Integer>(TFObjType.INT)
		{
			public void addInputVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.VAR_POS_A, INodeInput.makeInput(INodeInput.ofType(TFObjType.BLOCK, false)));
				set.put(CommonVariables.VAR_POS_B, INodeInput.makeInput(INodeInput.ofType(TFObjType.BLOCK, false), new WhiteboardObjBlock(), LocalWhiteboard.SELF.displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Integer> getResult(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				// Value A - mandatory
				IWhiteboardObject<?> objPosA = getOrDefault(CommonVariables.VAR_POS_A, parent, local, global);
				if(objPosA.isEmpty())
					return null;
				BlockPos posA = objPosA.as(TFObjType.BLOCK).get();
				
				// Value B - optional, defaults to mob's position
				IWhiteboardObject<?> objPosB = getOrDefault(CommonVariables.VAR_POS_B, parent, local, global);
				BlockPos posB;
				if(objPosB.isEmpty())
				{
					if(objPosB.size() == 0)
						posB = local.getValue(LocalWhiteboard.SELF).as(TFObjType.BLOCK).get();
					else
						return null;
				}
				else
					posB = objPosB.as(TFObjType.BLOCK).get();
				
				return new WhiteboardObj.Int((int)Math.sqrt(posA.getSquaredDistance(posB)));
			}
		});
		add(set, VARIANT_GET_BARK, new GetterHandler<Integer>(TFObjType.INT)
		{
			public void addInputVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.TARGET_ENT, INodeInput.makeInput(INodeInput.ofType(TFObjType.ENT, false), new WhiteboardObjEntity(), LocalWhiteboard.SELF.displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Integer> getResult(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<Entity> target = getOrDefault(CommonVariables.TARGET_ENT, parent, local, global).as(TFObjType.ENT);
				
				Entity entity = target.size() == 0 ? tricksy : target.get();
				if(!(entity instanceof ITricksyMob) || !entity.isAlive())
					return null;
				
				ITricksyMob<?> living = (ITricksyMob<?>)entity;
				return new WhiteboardObj.Int(living.currentBark().ordinal());
			}
		});
		add(set, VARIANT_ADD, new GetterHandler<Integer>(TFObjType.INT)
		{
			private static final WhiteboardRef SUB = new WhiteboardRef("subtract", TFObjType.BOOL).displayName(CommonVariables.translate("subtract"));
			
			public void addInputVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.VAR_A, INodeInput.makeInput(INodeInput.ofType(TFObjType.INT, false)));
				set.put(CommonVariables.VAR_B, INodeInput.makeInput(INodeInput.ofType(TFObjType.INT, false), new WhiteboardObj.Int(1), Text.literal(String.valueOf(1))));
				set.put(SUB, INodeInput.makeInput(INodeInput.ofType(TFObjType.BOOL, false), new WhiteboardObj.Bool(), Text.translatable("value."+Reference.ModInfo.MOD_ID+".boolean.false")));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Integer> getResult(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<Integer> intA = getOrDefault(CommonVariables.VAR_A, parent, local, global).as(TFObjType.INT);
				IWhiteboardObject<Integer> intB = getOrDefault(CommonVariables.VAR_B, parent, local, global).as(TFObjType.INT);
				IWhiteboardObject<Boolean> sub = getOrDefault(SUB, parent, local, global).as(TFObjType.BOOL);
				return new WhiteboardObj.Int(intA.get() + intB.get() * (sub.get() ? -1 : 1));
			}
		});
		add(set, VARIANT_OFFSET, new GetterHandler<BlockPos>(TFObjType.BLOCK)
		{
			public void addInputVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.VAR_A, INodeInput.makeInput(INodeInput.ofType(TFObjType.BLOCK, false)));
				set.put(CommonVariables.VAR_B, INodeInput.makeInput(INodeInput.ofType(TFObjType.BLOCK, true), new WhiteboardObjBlock(BlockPos.ORIGIN, Direction.NORTH), ConstantsWhiteboard.DIRECTIONS.get(Direction.NORTH).displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<BlockPos> getResult(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<BlockPos> intA = getOrDefault(CommonVariables.VAR_A, parent, local, global).as(TFObjType.BLOCK);
				IWhiteboardObject<BlockPos> intB = getOrDefault(CommonVariables.VAR_B, parent, local, global).as(TFObjType.BLOCK);
				
				BlockPos pos = intA.get();
				Direction face = ((WhiteboardObjBlock)intB).direction();
				return new WhiteboardObjBlock(pos.offset(face), ((WhiteboardObjBlock)intA).direction());
			}
		});
		return set;
	}
}