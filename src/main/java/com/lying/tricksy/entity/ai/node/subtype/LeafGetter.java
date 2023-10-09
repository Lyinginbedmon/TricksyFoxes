package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.handler.GetterHandler;
import com.lying.tricksy.entity.ai.node.handler.INodeInput;
import com.lying.tricksy.entity.ai.node.handler.InventoryHandler;
import com.lying.tricksy.entity.ai.node.handler.NodeTickHandler;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjEntity;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFObjType;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class LeafGetter implements ISubtypeGroup<LeafNode>
{
	// World scanning getters
	public static final Identifier VARIANT_GET_ITEM = ISubtypeGroup.variant("nearest_item");
	public static final Identifier VARIANT_GET_MONSTER = ISubtypeGroup.variant("nearest_hostile");
	public static final Identifier VARIANT_GET_DISTANCE = ISubtypeGroup.variant("distance_to");
	
	// Entity value getters
	public static final Identifier VARIANT_GET_ASSAILANT = ISubtypeGroup.variant("get_assailant");
	public static final Identifier VARIANT_GET_HEALTH = ISubtypeGroup.variant("get_health");
	public static final Identifier VARIANT_GET_HELD = ISubtypeGroup.variant("get_held_item");
	
	public void addActions(Collection<NodeSubType<LeafNode>> set)
	{
		add(set, VARIANT_GET_ITEM, new GetterHandler<Entity>(TFObjType.ENT)
		{
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.VAR_POS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK), new WhiteboardObjBlock(), LocalWhiteboard.SELF.displayName())); 
				set.put(CommonVariables.VAR_DIS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.INT), new WhiteboardObj.Int((int)NodeTickHandler.INTERACT_RANGE)));
				set.put(CommonVariables.VAR_ITEM, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.ITEM), new WhiteboardObj.Item()));
			}
			
			public <N extends PathAwareEntity & ITricksyMob<?>> @Nullable IWhiteboardObject<Entity> getResult(N tricksy, LocalWhiteboard<N> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<BlockPos> pos = getOrDefault(CommonVariables.VAR_POS, parent, local, global).as(TFObjType.BLOCK);
				IWhiteboardObject<Integer> range = getOrDefault(CommonVariables.VAR_DIS, parent, local, global).as(TFObjType.INT);
				IWhiteboardObject<ItemStack> filter = getOrDefault(CommonVariables.VAR_ITEM, parent, local, global).as(TFObjType.ITEM);
				
				int searchRange = MathHelper.clamp(range.get(), 0, 16);
				BlockPos point = pos.size() == 0 ? tricksy.getBlockPos() : pos.get();
				Box search = new Box(point).expand(searchRange);
				
				World world = tricksy.getWorld();
				if(search.minY < world.getBottomY())
					search = search.withMinY(world.getBottomY());
				
				List<ItemEntity> items = world.getEntitiesByClass(ItemEntity.class, search, (item) -> InventoryHandler.matchesItemFilter(item.getStack(), filter));
				if(items.isEmpty())
					return null;
				
				if(items.size() > 1)
					items.sort(new Comparator<ItemEntity>()
					{
						public int compare(ItemEntity o1, ItemEntity o2)
						{
							double dist1 = o1.distanceTo(tricksy);
							double dist2 = o2.distanceTo(tricksy);
							return dist1 < dist2 ? -1 : dist1 > dist2 ? 1 : 0;
						}
					});
				
				return new WhiteboardObjEntity(items.get(0));
			}
		});
		add(set, VARIANT_GET_MONSTER, new GetterHandler<Entity>(TFObjType.ENT)
		{
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.VAR_POS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK), new WhiteboardObjBlock(), LocalWhiteboard.SELF.displayName())); 
				set.put(CommonVariables.VAR_DIS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.INT), new WhiteboardObj.Int((int)NodeTickHandler.INTERACT_RANGE)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Entity> getResult(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<Integer> range = getOrDefault(CommonVariables.VAR_DIS, parent, local, global).as(TFObjType.INT);
				IWhiteboardObject<BlockPos> pos = getOrDefault(CommonVariables.VAR_POS, parent, local, global).as(TFObjType.BLOCK);
				
				int searchRange = MathHelper.clamp(range.get(), 0, 16);
				BlockPos point = pos.size() == 0 ? tricksy.getBlockPos() : pos.get();
				Box search = new Box(point).expand(searchRange);
				
				World world = tricksy.getWorld();
				if(search.minY < world.getBottomY())
					search = search.withMinY(world.getBottomY());
				
				List<MobEntity> monsters = world.getEntitiesByClass(MobEntity.class, search, (ent) -> ent.isAlive() && !ent.isSpectator() && ent instanceof Monster && ent != tricksy);
				if(monsters.isEmpty())
					return null;
				
				if(monsters.size() > 1)
					monsters.sort(new Comparator<MobEntity>()
					{
						public int compare(MobEntity o1, MobEntity o2)
						{
							double dist1 = o1.distanceTo(tricksy);
							double dist2 = o2.distanceTo(tricksy);
							return dist1 < dist2 ? -1 : dist1 > dist2 ? 1 : 0;
						}
					});
				return new WhiteboardObjEntity(monsters.get(0));
			}
		});
		add(set, VARIANT_GET_ASSAILANT, new GetterHandler<Entity>(TFObjType.ENT)
		{
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.TARGET_ENT, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.ENT), new WhiteboardObjEntity(), LocalWhiteboard.SELF.displayName()));
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
			
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.TARGET_ENT, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.ENT), new WhiteboardObjEntity(), LocalWhiteboard.SELF.displayName()));
				set.put(MAX, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BOOL), new WhiteboardObj.Bool(false)));
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
			
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.TARGET_ENT, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.ENT), new WhiteboardObjEntity(), LocalWhiteboard.SELF.displayName()));
				set.put(OFF, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BOOL), new WhiteboardObj.Bool(false)));
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
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.VAR_POS_A, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK)));
				set.put(CommonVariables.VAR_POS_B, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK), new WhiteboardObjBlock(), LocalWhiteboard.SELF.displayName()));
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
	}
}
