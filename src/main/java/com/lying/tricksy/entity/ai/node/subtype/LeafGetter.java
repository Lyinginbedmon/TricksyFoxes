package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.handler.GetterHandler;
import com.lying.tricksy.entity.ai.node.handler.INodeInput;
import com.lying.tricksy.entity.ai.node.handler.InventoryHandler;
import com.lying.tricksy.entity.ai.node.handler.NodeTickHandler;
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
import com.lying.tricksy.utility.Region;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class LeafGetter implements ISubtypeGroup<LeafNode>
{
	// World scanning getters
	public static final Identifier VARIANT_GET_ITEM = ISubtypeGroup.variant("nearest_item");
	public static final Identifier VARIANT_GET_ITEMS = ISubtypeGroup.variant("get_items");
	public static final Identifier VARIANT_GET_ENTITY = ISubtypeGroup.variant("nearest_creature");
	public static final Identifier VARIANT_GET_ENTITIES = ISubtypeGroup.variant("get_creatures");
	public static final Identifier VARIANT_GET_INVENTORY = ISubtypeGroup.variant("nearest_inventory");
	public static final Identifier VARIANT_GET_INVENTORIES = ISubtypeGroup.variant("get_inventories");
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
		add(set, VARIANT_GET_ITEM, new GetterHandler<Entity>(TFObjType.ENT)
		{
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.VAR_POS, INodeInput.makeInput((ref) -> !ref.isFilter() && (ref.type() == TFObjType.BLOCK || ref.type() == TFObjType.REGION), new WhiteboardObjBlock(), LocalWhiteboard.SELF.displayName()));
				set.put(CommonVariables.VAR_DIS, INodeInput.makeInput(INodeInput.ofType(TFObjType.INT, false), new WhiteboardObj.Int((int)NodeTickHandler.INTERACT_RANGE)));
				set.put(CommonVariables.VAR_ITEM, INodeInput.makeInput(INodeInput.ofType(TFObjType.ITEM, true), new WhiteboardObj.Item()));
			}
			
			public <N extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Entity> getResult(N tricksy, LocalWhiteboard<N> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<?> pos = getOrDefault(CommonVariables.VAR_POS, parent, local, global);
				IWhiteboardObject<Integer> range = getOrDefault(CommonVariables.VAR_DIS, parent, local, global).as(TFObjType.INT);
				IWhiteboardObject<ItemStack> filter = getOrDefault(CommonVariables.VAR_ITEM, parent, local, global).as(TFObjType.ITEM);
				
				Region searchArea = GetterHandler.getSearchArea(pos, range, tricksy);
				World world = tricksy.getWorld();
				List<ItemEntity> items = searchArea.getEntitiesByClass(ItemEntity.class, world, (item) -> InventoryHandler.matchesItemFilter(item.getStack(), filter));
				if(items.isEmpty())
					return null;
				else
					items.sort(new Comparator<ItemEntity>()
							{
								public int compare(ItemEntity o1, ItemEntity o2)
								{
									BlockPos center = searchArea.center();
									double dist1 = o1.squaredDistanceTo(center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D);
									double dist2 = o2.squaredDistanceTo(center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D);
									return dist1 < dist2 ? -1 : dist1 > dist2 ? 1 : 0;
								}
							});
				
				return new WhiteboardObjEntity(items.get(0));
			}
		});
		add(set, VARIANT_GET_ITEMS, new GetterHandler<Entity>(TFObjType.ENT)
		{
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.VAR_POS, GetterHandler.POS_OR_REGION);
				set.put(CommonVariables.VAR_DIS, INodeInput.makeInput(INodeInput.ofType(TFObjType.INT, false), new WhiteboardObj.Int((int)NodeTickHandler.INTERACT_RANGE)));
				set.put(CommonVariables.VAR_ITEM, INodeInput.makeInput(INodeInput.ofType(TFObjType.ITEM, true), new WhiteboardObj.Item()));
			}
			
			public <N extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Entity> getResult(N tricksy, LocalWhiteboard<N> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<?> pos = getOrDefault(CommonVariables.VAR_POS, parent, local, global);
				IWhiteboardObject<Integer> range = getOrDefault(CommonVariables.VAR_DIS, parent, local, global).as(TFObjType.INT);
				IWhiteboardObject<ItemStack> filter = getOrDefault(CommonVariables.VAR_ITEM, parent, local, global).as(TFObjType.ITEM);
				
				Region searchArea = GetterHandler.getSearchArea(pos, range, tricksy);
				World world = tricksy.getWorld();
				List<ItemEntity> items = searchArea.getEntitiesByClass(ItemEntity.class, world, (item) -> InventoryHandler.matchesItemFilter(item.getStack(), filter));
				if(items.isEmpty())
					return null;
				else
					items.sort(new Comparator<ItemEntity>()
							{
								public int compare(ItemEntity o1, ItemEntity o2)
								{
									BlockPos center = searchArea.center();
									double dist1 = o1.squaredDistanceTo(center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D);
									double dist2 = o2.squaredDistanceTo(center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D);
									return dist1 < dist2 ? -1 : dist1 > dist2 ? 1 : 0;
								}
							});
				
				WhiteboardObjEntity result = new WhiteboardObjEntity();
				items.forEach((mob) -> result.add(mob));
				return result;
			}
		});
		add(set, VARIANT_GET_ENTITY, new GetterHandler<Entity>(TFObjType.ENT)
		{
			public static WhiteboardRef FILTER = new WhiteboardRef("entity_filter", TFObjType.ENT).displayName(CommonVariables.translate("item_filter"));
			
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.VAR_POS, GetterHandler.POS_OR_REGION);
				set.put(CommonVariables.VAR_DIS, INodeInput.makeInput(INodeInput.ofType(TFObjType.INT, false), new WhiteboardObj.Int((int)NodeTickHandler.INTERACT_RANGE)));
				set.put(FILTER, INodeInput.makeInput(INodeInput.ofType(TFObjType.ENT, true), ConstantsWhiteboard.FILTER_MONSTER.copy(), ConstantsWhiteboard.ENT_MONSTERS.displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Entity> getResult(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<?> pos = getOrDefault(CommonVariables.VAR_POS, parent, local, global);
				IWhiteboardObject<Integer> range = getOrDefault(CommonVariables.VAR_DIS, parent, local, global).as(TFObjType.INT);
				IWhiteboardObject<Entity> filter = getOrDefault(FILTER, parent, local, global).as(TFObjType.ENT);
				
				Region searchArea = GetterHandler.getSearchArea(pos, range, tricksy);
				World world = tricksy.getWorld();
				List<LivingEntity> mobs = searchArea.getEntitiesByClass(LivingEntity.class, world, (ent) -> NodeTickHandler.matchesEntityFilter(ent, filter));
				if(mobs.isEmpty())
					return null;
				else
					mobs.sort(new Comparator<LivingEntity>()
							{
								public int compare(LivingEntity o1, LivingEntity o2)
								{
									BlockPos center = searchArea.center();
									double dist1 = o1.squaredDistanceTo(center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D);
									double dist2 = o2.squaredDistanceTo(center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D);
									return dist1 < dist2 ? -1 : dist1 > dist2 ? 1 : 0;
								}
							});
				return new WhiteboardObjEntity(mobs.get(0));
			}
		});
		add(set, VARIANT_GET_ENTITIES, new GetterHandler<Entity>(TFObjType.ENT)
		{
			public static WhiteboardRef FILTER = new WhiteboardRef("entity_filter", TFObjType.ENT).displayName(CommonVariables.translate("item_filter"));
			
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.VAR_POS, GetterHandler.POS_OR_REGION);
				set.put(CommonVariables.VAR_DIS, INodeInput.makeInput(INodeInput.ofType(TFObjType.INT, false), new WhiteboardObj.Int((int)NodeTickHandler.INTERACT_RANGE)));
				set.put(FILTER, INodeInput.makeInput(INodeInput.ofType(TFObjType.ENT, true), ConstantsWhiteboard.FILTER_MONSTER.copy(), ConstantsWhiteboard.ENT_MONSTERS.displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Entity> getResult(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<?> pos = getOrDefault(CommonVariables.VAR_POS, parent, local, global);
				IWhiteboardObject<Integer> range = getOrDefault(CommonVariables.VAR_DIS, parent, local, global).as(TFObjType.INT);
				IWhiteboardObject<Entity> filter = getOrDefault(FILTER, parent, local, global).as(TFObjType.ENT);
				
				Region searchArea = GetterHandler.getSearchArea(pos, range, tricksy);
				World world = tricksy.getWorld();
				List<LivingEntity> mobs = searchArea.getEntitiesByClass(LivingEntity.class, world, (ent) -> NodeTickHandler.matchesEntityFilter(ent, filter));
				if(mobs.isEmpty())
					return null;
				else
					mobs.sort(new Comparator<LivingEntity>()
							{
								public int compare(LivingEntity o1, LivingEntity o2)
								{
									BlockPos center = searchArea.center();
									double dist1 = o1.squaredDistanceTo(center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D);
									double dist2 = o2.squaredDistanceTo(center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D);
									return dist1 < dist2 ? -1 : dist1 > dist2 ? 1 : 0;
								}
							});
				
				WhiteboardObjEntity result = new WhiteboardObjEntity();
				mobs.forEach((mob) -> result.add(mob));
				return result;
			}
		});
		add(set, VARIANT_GET_ASSAILANT, new GetterHandler<Entity>(TFObjType.ENT)
		{
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
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
			
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
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
			
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
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
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
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
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
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
			
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.VAR_A, INodeInput.makeInput(INodeInput.ofType(TFObjType.INT, false)));
				set.put(CommonVariables.VAR_B, INodeInput.makeInput(INodeInput.ofType(TFObjType.INT, false), new WhiteboardObj.Int(1), ConstantsWhiteboard.NUM_1.displayName()));
				set.put(SUB, INodeInput.makeInput(INodeInput.ofType(TFObjType.BOOL, false), new WhiteboardObj.Bool(), ConstantsWhiteboard.BOOL_FALSE.displayName()));
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
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
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
		add(set, VARIANT_GET_INVENTORY, new GetterHandler<BlockPos>(TFObjType.BLOCK)
		{
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.VAR_POS, GetterHandler.POS_OR_REGION);
				set.put(CommonVariables.VAR_DIS, INodeInput.makeInput(INodeInput.ofType(TFObjType.INT, false), new WhiteboardObj.Int((int)NodeTickHandler.INTERACT_RANGE)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<BlockPos> getResult(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<?> pos = getOrDefault(CommonVariables.VAR_POS, parent, local, global);
				IWhiteboardObject<Integer> range = getOrDefault(CommonVariables.VAR_DIS, parent, local, global).as(TFObjType.INT);
				
				Region searchArea = GetterHandler.getSearchArea(pos, range, tricksy);
				World world = tricksy.getWorld();
				List<BlockPos> chests = getInventoriesWithin(searchArea, world);
				return chests.isEmpty() ? null : new WhiteboardObjBlock(chests.get(0));
			}
		});
		add(set, VARIANT_GET_INVENTORIES, new GetterHandler<BlockPos>(TFObjType.BLOCK)
		{
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.VAR_POS, GetterHandler.POS_OR_REGION);
				set.put(CommonVariables.VAR_DIS, INodeInput.makeInput(INodeInput.ofType(TFObjType.INT, false), new WhiteboardObj.Int((int)NodeTickHandler.INTERACT_RANGE)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<BlockPos> getResult(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<?> pos = getOrDefault(CommonVariables.VAR_POS, parent, local, global);
				IWhiteboardObject<Integer> range = getOrDefault(CommonVariables.VAR_DIS, parent, local, global).as(TFObjType.INT);
				
				Region searchArea = GetterHandler.getSearchArea(pos, range, tricksy);
				World world = tricksy.getWorld();
				List<BlockPos> chests = getInventoriesWithin(searchArea, world);
				
				WhiteboardObjBlock result = new WhiteboardObjBlock();
				chests.forEach((mob) -> result.add(mob));
				return result;
			}
		});
		return set;
	}
	
	private static List<BlockPos> getInventoriesWithin(Region area, World world)
	{
		List<BlockPos> inventories = area.getBlocks(world, (pos,state) -> state.hasBlockEntity() && world.getBlockEntity(pos) instanceof Inventory);
		if(inventories.isEmpty())
			return inventories;
		
		if(inventories.size() > 1)
			inventories.sort(new Comparator<BlockPos>()
			{
				public int compare(BlockPos o1, BlockPos o2)
				{
					double dist1 = o1.getSquaredDistance(area.center());
					double dist2 = o2.getSquaredDistance(area.center());
					return dist1 < dist2 ? -1 : dist1 > dist2 ? 1 : 0;
				}
			});
		return inventories;
	}
}