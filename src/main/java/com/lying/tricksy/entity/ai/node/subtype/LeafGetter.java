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
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardManager;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjEntity;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class LeafGetter extends NodeGroupLeaf
{
	public static NodeSubType<LeafNode> GET_DISTANCE;
	
	// Entity value getters
	public static NodeSubType<LeafNode> GET_ASSAILANT;
	public static NodeSubType<LeafNode> GET_HEALTH;
	public static NodeSubType<LeafNode> GET_HELD;
	public static NodeSubType<LeafNode> GET_BARK;
	public static NodeSubType<LeafNode> GET_LEASHED;
	
	public Identifier getRegistryName() { return new Identifier(Reference.ModInfo.MOD_ID, "leaf_getter"); }
	
	public Collection<NodeSubType<LeafNode>> getSubtypes()
	{
		List<NodeSubType<LeafNode>> set = Lists.newArrayList();
		set.add(GET_ASSAILANT = subtype(ISubtypeGroup.variant("get_assailant"), new GetterHandlerTyped<Entity>(TFObjType.ENT)
		{
			public void addInputVariables(Map<WhiteboardRef, INodeIO> set)
			{
				set.put(CommonVariables.TARGET_ENT, NodeInput.makeInput(NodeInput.ofType(TFObjType.ENT, false), new WhiteboardObjEntity(), LocalWhiteboard.SELF.displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Entity> getTypedResult(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				IWhiteboardObject<Entity> target = getOrDefault(CommonVariables.TARGET_ENT, parent, whiteboards).as(TFObjType.ENT);
				
				Entity entity = target.size() == 0 ? tricksy : target.get();
				if(!(entity instanceof LivingEntity))
					return null;
				
				Entity assailant = ((LivingEntity)entity).getAttacker();
				return assailant == null || assailant == tricksy ? new WhiteboardObjEntity() : new WhiteboardObjEntity(assailant);
			}
		}));
		set.add(GET_HEALTH = subtype(ISubtypeGroup.variant("get_health"), new GetterHandlerTyped<Integer>(TFObjType.INT)
		{
			private static final WhiteboardRef MAX = new WhiteboardRef("max_health", TFObjType.BOOL).displayName(CommonVariables.translate("max_health"));
			
			public void addInputVariables(Map<WhiteboardRef, INodeIO> set)
			{
				set.put(CommonVariables.TARGET_ENT, NodeInput.makeInput(NodeInput.ofType(TFObjType.ENT, false), new WhiteboardObjEntity(), LocalWhiteboard.SELF.displayName()));
				set.put(MAX, NodeInput.makeInput(NodeInput.ofType(TFObjType.BOOL, false), new WhiteboardObj.Bool(false)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Integer> getTypedResult(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				IWhiteboardObject<Entity> target = getOrDefault(CommonVariables.TARGET_ENT, parent, whiteboards).as(TFObjType.ENT);
				IWhiteboardObject<Boolean> isMax = getOrDefault(MAX, parent, whiteboards).as(TFObjType.BOOL);
				
				Entity entity = target.size() == 0 ? tricksy : target.get();
				if(!(entity instanceof LivingEntity) || !entity.isAlive())
					return null;
				
				LivingEntity living = (LivingEntity)entity;
				return new WhiteboardObj.Int((int)(isMax.get() ? living.getMaxHealth() : living.getHealth()));
			}
		}));
		set.add(GET_HELD = subtype(ISubtypeGroup.variant("get_held_item"), new GetterHandlerTyped<ItemStack>(TFObjType.ITEM)
		{
			private static final WhiteboardRef OFF = new WhiteboardRef("offhand", TFObjType.BOOL).displayName(CommonVariables.translate("offhand"));
			
			public void addInputVariables(Map<WhiteboardRef, INodeIO> set)
			{
				set.put(CommonVariables.TARGET_ENT, NodeInput.makeInput(NodeInput.ofType(TFObjType.ENT, false), new WhiteboardObjEntity(), LocalWhiteboard.SELF.displayName()));
				set.put(OFF, NodeInput.makeInput(NodeInput.ofType(TFObjType.BOOL, false), new WhiteboardObj.Bool(false)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<ItemStack> getTypedResult(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				IWhiteboardObject<Entity> target = getOrDefault(CommonVariables.TARGET_ENT, parent, whiteboards).as(TFObjType.ENT);
				IWhiteboardObject<Boolean> isOff = getOrDefault(OFF, parent, whiteboards).as(TFObjType.BOOL);
				
				Entity entity = target.size() == 0 ? tricksy : target.get();
				if(!(entity instanceof LivingEntity))
					return null;
				
				LivingEntity living = (LivingEntity)entity;
				return new WhiteboardObj.Item(isOff.get() ? living.getOffHandStack() : living.getMainHandStack());
			}
		}));
		set.add(GET_DISTANCE = subtype(ISubtypeGroup.variant("distance_to"), new GetterHandlerTyped<Integer>(TFObjType.INT)
		{
			public void addInputVariables(Map<WhiteboardRef, INodeIO> set)
			{
				set.put(CommonVariables.VAR_POS_A, NodeInput.makeInput(NodeInput.ofType(TFObjType.BLOCK, false)));
				set.put(CommonVariables.VAR_POS_B, NodeInput.makeInput(NodeInput.ofType(TFObjType.BLOCK, false), new WhiteboardObjBlock(), LocalWhiteboard.SELF.displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Integer> getTypedResult(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				// Value A - mandatory
				IWhiteboardObject<?> objPosA = getOrDefault(CommonVariables.VAR_POS_A, parent, whiteboards);
				if(objPosA.isEmpty())
					return null;
				BlockPos posA = objPosA.as(TFObjType.BLOCK).get();
				
				// Value B - optional, defaults to mob's position
				IWhiteboardObject<?> objPosB = getOrDefault(CommonVariables.VAR_POS_B, parent, whiteboards);
				BlockPos posB;
				if(objPosB.isEmpty())
				{
					if(objPosB.size() == 0)
						posB = whiteboards.local().getValue(LocalWhiteboard.SELF).as(TFObjType.BLOCK).get();
					else
						return null;
				}
				else
					posB = objPosB.as(TFObjType.BLOCK).get();
				
				return new WhiteboardObj.Int((int)Math.sqrt(posA.getSquaredDistance(posB)));
			}
		}));
		set.add(GET_BARK = subtype(ISubtypeGroup.variant("get_bark"), new GetterHandlerTyped<Integer>(TFObjType.INT)
		{
			public void addInputVariables(Map<WhiteboardRef, INodeIO> set)
			{
				set.put(CommonVariables.TARGET_ENT, NodeInput.makeInput(NodeInput.ofType(TFObjType.ENT, false), new WhiteboardObjEntity(), LocalWhiteboard.SELF.displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Integer> getTypedResult(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				IWhiteboardObject<Entity> target = getOrDefault(CommonVariables.TARGET_ENT, parent, whiteboards).as(TFObjType.ENT);
				
				Entity entity = target.size() == 0 ? tricksy : target.get();
				if(!(entity instanceof ITricksyMob) || !entity.isAlive())
					return null;
				
				ITricksyMob<?> living = (ITricksyMob<?>)entity;
				return new WhiteboardObj.Int(living.currentBark().ordinal());
			}
		}));
		set.add(GET_LEASHED = subtype(ISubtypeGroup.variant("get_leashed"), new GetterHandlerTyped<Entity>(TFObjType.ENT)
			{
				private static final WhiteboardRef TARGET = CommonVariables.TARGET_ENT;
				private static final WhiteboardRef INVERT = new WhiteboardRef("invert", TFObjType.BOOL).displayName(CommonVariables.translate("invert"));
				
				public void addInputVariables(Map<WhiteboardRef, INodeIO> set)
				{
					set.put(TARGET, NodeInput.makeInput(NodeInput.ofType(TFObjType.ENT, false), new WhiteboardObjEntity(), LocalWhiteboard.SELF.displayName()));
					set.put(INVERT, NodeInput.makeInput(NodeInput.ofType(TFObjType.BOOL, true), new WhiteboardObj.Bool(false), (new WhiteboardObj.Bool(false)).describe().get(0)));
				}
				
				public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Entity> getTypedResult(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
				{
					IWhiteboardObject<Entity> target = getOrDefault(TARGET, parent, whiteboards).as(TFObjType.ENT);
					if(parent.inputAssigned(TARGET) && target.isEmpty())
						return null;
					
					Entity entity = parent.inputAssigned(TARGET) ? target.get() : tricksy;
					if(!entity.isAlive())
						return null;
					
					if(getOrDefault(INVERT, parent, whiteboards).as(TFObjType.BOOL).get())
					{
						WhiteboardObjEntity result = new WhiteboardObjEntity();
						tricksy.getWorld().getNonSpectatingEntities(MobEntity.class, (new Box(tricksy.getBlockPos())).expand(7D)).forEach(mob -> 
						{
							Entity holder = mob.getHoldingEntity();
							if(holder == entity)
								result.add(mob);
						});
						return result;
					}
					else if(entity instanceof MobEntity)
						return new WhiteboardObjEntity(((MobEntity)entity).getHoldingEntity());
					else
						return null;
				}
			}));
		return set;
	}
}