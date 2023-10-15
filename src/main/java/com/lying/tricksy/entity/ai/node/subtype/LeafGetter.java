package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

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
import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjEntity;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
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
	
	public Text displayName() { return Text.translatable("subtype."+Reference.ModInfo.MOD_ID+".leaf_getter"); }

	public Collection<NodeSubType<LeafNode>> getSubtypes()
	{
		List<NodeSubType<LeafNode>> set = Lists.newArrayList();
		add(set, VARIANT_GET_ITEM, new GetterHandler<Entity>(TFObjType.ENT)
		{
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.VAR_POS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK, false), new WhiteboardObjBlock(), LocalWhiteboard.SELF.displayName())); 
				set.put(CommonVariables.VAR_DIS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.INT, false), new WhiteboardObj.Int((int)NodeTickHandler.INTERACT_RANGE)));
				set.put(CommonVariables.VAR_ITEM, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.ITEM, true), new WhiteboardObj.Item()));
			}
			
			public <N extends PathAwareEntity & ITricksyMob<?>> @Nullable IWhiteboardObject<Entity> getResult(N tricksy, LocalWhiteboard<N> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<BlockPos> pos = getOrDefault(CommonVariables.VAR_POS, parent, local, global).as(TFObjType.BLOCK);
				IWhiteboardObject<Integer> range = getOrDefault(CommonVariables.VAR_DIS, parent, local, global).as(TFObjType.INT);
				IWhiteboardObject<ItemStack> filter = getOrDefault(CommonVariables.VAR_ITEM, parent, local, global).as(TFObjType.ITEM);
				
				BlockPos point = pos.size() == 0 ? tricksy.getBlockPos() : pos.get();
				World world = tricksy.getWorld();
				List<ItemEntity> items = getEntitiesWithin(ItemEntity.class, point, range.get(), world, tricksy, (item) -> InventoryHandler.matchesItemFilter(item.getStack(), filter));
				if(items.isEmpty())
					return null;
				
				return new WhiteboardObjEntity(items.get(0));
			}
		});
		add(set, VARIANT_GET_ITEMS, new GetterHandler<Entity>(TFObjType.ENT)
		{
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.VAR_POS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK, false), new WhiteboardObjBlock(), LocalWhiteboard.SELF.displayName())); 
				set.put(CommonVariables.VAR_DIS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.INT, false), new WhiteboardObj.Int((int)NodeTickHandler.INTERACT_RANGE)));
				set.put(CommonVariables.VAR_ITEM, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.ITEM, true), new WhiteboardObj.Item()));
			}
			
			public <N extends PathAwareEntity & ITricksyMob<?>> @Nullable IWhiteboardObject<Entity> getResult(N tricksy, LocalWhiteboard<N> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<BlockPos> pos = getOrDefault(CommonVariables.VAR_POS, parent, local, global).as(TFObjType.BLOCK);
				IWhiteboardObject<Integer> range = getOrDefault(CommonVariables.VAR_DIS, parent, local, global).as(TFObjType.INT);
				IWhiteboardObject<ItemStack> filter = getOrDefault(CommonVariables.VAR_ITEM, parent, local, global).as(TFObjType.ITEM);
				
				BlockPos point = pos.size() == 0 ? tricksy.getBlockPos() : pos.get();
				World world = tricksy.getWorld();
				List<ItemEntity> items = getEntitiesWithin(ItemEntity.class, point, range.get(), world, tricksy, (item) -> InventoryHandler.matchesItemFilter(item.getStack(), filter));
				if(items.isEmpty())
					return null;
				
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
				set.put(CommonVariables.VAR_POS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK, false), new WhiteboardObjBlock(), LocalWhiteboard.SELF.displayName())); 
				set.put(CommonVariables.VAR_DIS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.INT, false), new WhiteboardObj.Int((int)NodeTickHandler.INTERACT_RANGE)));
				set.put(FILTER, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.ENT, true), ConstantsWhiteboard.FILTER_MONSTER.copy(), ConstantsWhiteboard.ENT_MONSTERS.displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Entity> getResult(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<Integer> range = getOrDefault(CommonVariables.VAR_DIS, parent, local, global).as(TFObjType.INT);
				IWhiteboardObject<BlockPos> pos = getOrDefault(CommonVariables.VAR_POS, parent, local, global).as(TFObjType.BLOCK);
				IWhiteboardObject<Entity> filter = getOrDefault(FILTER, parent, local, global).as(TFObjType.ENT);
				
				BlockPos point = pos.size() == 0 ? tricksy.getBlockPos() : pos.get();
				World world = tricksy.getWorld();
				List<LivingEntity> mobs = getEntitiesWithin(LivingEntity.class, point, range.get(), world, tricksy, (ent) -> NodeTickHandler.matchesEntityFilter(ent, filter));
				if(mobs == null)
					return null;
				return new WhiteboardObjEntity(mobs.get(0));
			}
		});
		add(set, VARIANT_GET_ENTITIES, new GetterHandler<Entity>(TFObjType.ENT)
		{
			public static WhiteboardRef FILTER = new WhiteboardRef("entity_filter", TFObjType.ENT).displayName(CommonVariables.translate("item_filter"));
			
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.VAR_POS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK, false), new WhiteboardObjBlock(), LocalWhiteboard.SELF.displayName())); 
				set.put(CommonVariables.VAR_DIS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.INT, false), new WhiteboardObj.Int((int)NodeTickHandler.INTERACT_RANGE)));
				set.put(FILTER, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.ENT, true), ConstantsWhiteboard.FILTER_MONSTER.copy(), ConstantsWhiteboard.ENT_MONSTERS.displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Entity> getResult(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<Integer> range = getOrDefault(CommonVariables.VAR_DIS, parent, local, global).as(TFObjType.INT);
				IWhiteboardObject<BlockPos> pos = getOrDefault(CommonVariables.VAR_POS, parent, local, global).as(TFObjType.BLOCK);
				IWhiteboardObject<Entity> filter = getOrDefault(FILTER, parent, local, global).as(TFObjType.ENT);
				
				BlockPos point = pos.size() == 0 ? tricksy.getBlockPos() : pos.get();
				World world = tricksy.getWorld();
				List<LivingEntity> mobs = getEntitiesWithin(LivingEntity.class, point, range.get(), world, tricksy, (ent) -> NodeTickHandler.matchesEntityFilter(ent, filter));
				if(mobs == null)
					return null;
				
				WhiteboardObjEntity result = new WhiteboardObjEntity();
				mobs.forEach((mob) -> result.add(mob));
				return result;
			}
		});
		add(set, VARIANT_GET_ASSAILANT, new GetterHandler<Entity>(TFObjType.ENT)
		{
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.TARGET_ENT, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.ENT, false), new WhiteboardObjEntity(), LocalWhiteboard.SELF.displayName()));
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
				set.put(CommonVariables.TARGET_ENT, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.ENT, false), new WhiteboardObjEntity(), LocalWhiteboard.SELF.displayName()));
				set.put(MAX, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BOOL, false), new WhiteboardObj.Bool(false)));
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
				set.put(CommonVariables.TARGET_ENT, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.ENT, false), new WhiteboardObjEntity(), LocalWhiteboard.SELF.displayName()));
				set.put(OFF, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BOOL, false), new WhiteboardObj.Bool(false)));
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
				set.put(CommonVariables.VAR_POS_A, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK, false)));
				set.put(CommonVariables.VAR_POS_B, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK, false), new WhiteboardObjBlock(), LocalWhiteboard.SELF.displayName()));
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
				set.put(CommonVariables.TARGET_ENT, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.ENT, false), new WhiteboardObjEntity(), LocalWhiteboard.SELF.displayName()));
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
				set.put(CommonVariables.VAR_A, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.INT, false)));
				set.put(CommonVariables.VAR_B, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.INT, false), new WhiteboardObj.Int(1), ConstantsWhiteboard.NUM_1.displayName()));
				set.put(SUB, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BOOL, false), new WhiteboardObj.Bool(), ConstantsWhiteboard.BOOL_FALSE.displayName()));
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
				set.put(CommonVariables.VAR_A, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK, false)));
				set.put(CommonVariables.VAR_B, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK, true), new WhiteboardObjBlock(BlockPos.ORIGIN, Direction.NORTH), ConstantsWhiteboard.DIRECTIONS.get(Direction.NORTH).displayName()));
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
				set.put(CommonVariables.VAR_POS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK, false), new WhiteboardObjBlock(), LocalWhiteboard.SELF.displayName())); 
				set.put(CommonVariables.VAR_DIS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.INT, false), new WhiteboardObj.Int((int)NodeTickHandler.INTERACT_RANGE)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<BlockPos> getResult(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<Integer> range = getOrDefault(CommonVariables.VAR_DIS, parent, local, global).as(TFObjType.INT);
				IWhiteboardObject<BlockPos> pos = getOrDefault(CommonVariables.VAR_POS, parent, local, global).as(TFObjType.BLOCK);
				
				BlockPos point = pos.size() == 0 ? tricksy.getBlockPos() : pos.get();
				World world = tricksy.getWorld();
				List<BlockPos> chests = getInventoriesWithin(point, range.get(), world);
				if(chests == null)
					return null;
				
				return new WhiteboardObjBlock(chests.get(0));
			}
		});
		add(set, VARIANT_GET_INVENTORIES, new GetterHandler<BlockPos>(TFObjType.BLOCK)
		{
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.VAR_POS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK, false), new WhiteboardObjBlock(), LocalWhiteboard.SELF.displayName())); 
				set.put(CommonVariables.VAR_DIS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.INT, false), new WhiteboardObj.Int((int)NodeTickHandler.INTERACT_RANGE)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<BlockPos> getResult(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<Integer> range = getOrDefault(CommonVariables.VAR_DIS, parent, local, global).as(TFObjType.INT);
				IWhiteboardObject<BlockPos> pos = getOrDefault(CommonVariables.VAR_POS, parent, local, global).as(TFObjType.BLOCK);
				
				BlockPos point = pos.size() == 0 ? tricksy.getBlockPos() : pos.get();
				World world = tricksy.getWorld();
				List<BlockPos> chests = getInventoriesWithin(point, range.get(), world);
				if(chests == null)
					return null;
				
				WhiteboardObjBlock result = new WhiteboardObjBlock();
				chests.forEach((mob) -> result.add(mob));
				return result;
			}
		});
		return set;
	}
	
	@Nullable
	private static <T extends Entity> List<T> getEntitiesWithin(Class<T> classIn, BlockPos point, int distance, World world, Entity tricksy, Predicate<T> predicate)
	{
		int searchRange = MathHelper.clamp(distance, 0, 16);
		if(searchRange == 0)
			return null;
		
		Box search = new Box(point).expand(searchRange);
		if(search.minY < world.getBottomY())
			search = search.withMinY(world.getBottomY());
		
		List<T> mobs = world.getEntitiesByClass(classIn, search, (ent) -> ent.isAlive() && ent != tricksy && !ent.isSpectator() && predicate.test(ent));
		if(mobs.isEmpty())
			return null;
		
		if(mobs.size() > 1)
			mobs.sort(new Comparator<T>()
			{
				public int compare(T o1, T o2)
				{
					double dist1 = o1.getBlockPos().getSquaredDistance(point);
					double dist2 = o2.getBlockPos().getSquaredDistance(point);
					return dist1 < dist2 ? -1 : dist1 > dist2 ? 1 : 0;
				}
			});
		return mobs;
	}
	
	@Nullable
	private static List<BlockPos> getInventoriesWithin(BlockPos point, int distance, World world)
	{
		int searchRange = MathHelper.clamp(distance, 0, 4);
		if(searchRange == 0)
			return null;
		
		Box search = new Box(point).expand(searchRange);
		if(search.minY < world.getBottomY())
			search = search.withMinY(world.getBottomY());
		
		List<BlockPos> inventories = Lists.newArrayList();
		for(int y=(int)search.minY; y < search.maxY; y++)
			for(int x=(int)search.minX; x < search.maxX; x++)
				for(int z=(int)search.minZ; z< search.maxZ; z++)
				{
					BlockPos offset = new BlockPos(x, y, z);
					if(world.getBlockEntity(offset) instanceof Inventory)
						inventories.add(offset);
				}
		
		if(inventories.isEmpty())
			return null;
		
		if(inventories.size() > 1)
			inventories.sort(new Comparator<BlockPos>()
			{
				public int compare(BlockPos o1, BlockPos o2)
				{
					double dist1 = o1.getSquaredDistance(point);
					double dist2 = o2.getSquaredDistance(point);
					return dist1 < dist2 ? -1 : dist1 > dist2 ? 1 : 0;
				}
			});
		return inventories;
	}
}
