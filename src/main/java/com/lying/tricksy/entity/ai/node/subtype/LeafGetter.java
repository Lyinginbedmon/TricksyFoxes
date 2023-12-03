package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.api.entity.ai.INodeIO;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.handler.GetterHandlerTyped;
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
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class LeafGetter implements ISubtypeGroup<LeafNode>
{
	public static final Identifier VARIANT_GET_DISTANCE = ISubtypeGroup.variant("distance_to");
	
	// Entity value getters
	public static final Identifier VARIANT_GET_ASSAILANT = ISubtypeGroup.variant("get_assailant");
	public static final Identifier VARIANT_GET_HEALTH = ISubtypeGroup.variant("get_health");
	public static final Identifier VARIANT_GET_HELD = ISubtypeGroup.variant("get_held_item");
	public static final Identifier VARIANT_GET_BARK = ISubtypeGroup.variant("get_bark");
	
	public Identifier getRegistryName() { return new Identifier(Reference.ModInfo.MOD_ID, "leaf_getter"); }
	
	public Collection<NodeSubType<LeafNode>> getSubtypes()
	{
		List<NodeSubType<LeafNode>> set = Lists.newArrayList();
		add(set, VARIANT_GET_ASSAILANT, new GetterHandlerTyped<Entity>(TFObjType.ENT)
		{
			public void addInputVariables(Map<WhiteboardRef, INodeIO> set)
			{
				set.put(CommonVariables.TARGET_ENT, NodeInput.makeInput(NodeInput.ofType(TFObjType.ENT, false), new WhiteboardObjEntity(), LocalWhiteboard.SELF.displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Entity> getTypedResult(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<Entity> target = getOrDefault(CommonVariables.TARGET_ENT, parent, local, global).as(TFObjType.ENT);
				
				Entity entity = target.size() == 0 ? tricksy : target.get();
				if(!(entity instanceof LivingEntity))
					return null;
				
				Entity assailant = ((LivingEntity)entity).getAttacker();
				return assailant == null || assailant == tricksy ? new WhiteboardObjEntity() : new WhiteboardObjEntity(assailant);
			}
		});
		add(set, VARIANT_GET_HEALTH, new GetterHandlerTyped<Integer>(TFObjType.INT)
		{
			private static final WhiteboardRef MAX = new WhiteboardRef("max_health", TFObjType.BOOL).displayName(CommonVariables.translate("max_health"));
			
			public void addInputVariables(Map<WhiteboardRef, INodeIO> set)
			{
				set.put(CommonVariables.TARGET_ENT, NodeInput.makeInput(NodeInput.ofType(TFObjType.ENT, false), new WhiteboardObjEntity(), LocalWhiteboard.SELF.displayName()));
				set.put(MAX, NodeInput.makeInput(NodeInput.ofType(TFObjType.BOOL, false), new WhiteboardObj.Bool(false)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Integer> getTypedResult(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
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
		add(set, VARIANT_GET_HELD, new GetterHandlerTyped<ItemStack>(TFObjType.ITEM)
		{
			private static final WhiteboardRef OFF = new WhiteboardRef("offhand", TFObjType.BOOL).displayName(CommonVariables.translate("offhand"));
			
			public void addInputVariables(Map<WhiteboardRef, INodeIO> set)
			{
				set.put(CommonVariables.TARGET_ENT, NodeInput.makeInput(NodeInput.ofType(TFObjType.ENT, false), new WhiteboardObjEntity(), LocalWhiteboard.SELF.displayName()));
				set.put(OFF, NodeInput.makeInput(NodeInput.ofType(TFObjType.BOOL, false), new WhiteboardObj.Bool(false)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<ItemStack> getTypedResult(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
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
		add(set, VARIANT_GET_DISTANCE, new GetterHandlerTyped<Integer>(TFObjType.INT)
		{
			public void addInputVariables(Map<WhiteboardRef, INodeIO> set)
			{
				set.put(CommonVariables.VAR_POS_A, NodeInput.makeInput(NodeInput.ofType(TFObjType.BLOCK, false)));
				set.put(CommonVariables.VAR_POS_B, NodeInput.makeInput(NodeInput.ofType(TFObjType.BLOCK, false), new WhiteboardObjBlock(), LocalWhiteboard.SELF.displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Integer> getTypedResult(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
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
		add(set, VARIANT_GET_BARK, new GetterHandlerTyped<Integer>(TFObjType.INT)
		{
			public void addInputVariables(Map<WhiteboardRef, INodeIO> set)
			{
				set.put(CommonVariables.TARGET_ENT, NodeInput.makeInput(NodeInput.ofType(TFObjType.ENT, false), new WhiteboardObjEntity(), LocalWhiteboard.SELF.displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Integer> getTypedResult(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<Entity> target = getOrDefault(CommonVariables.TARGET_ENT, parent, local, global).as(TFObjType.ENT);
				
				Entity entity = target.size() == 0 ? tricksy : target.get();
				if(!(entity instanceof ITricksyMob) || !entity.isAlive())
					return null;
				
				ITricksyMob<?> living = (ITricksyMob<?>)entity;
				return new WhiteboardObj.Int(living.currentBark().ordinal());
			}
		});
		return set;
	}
}