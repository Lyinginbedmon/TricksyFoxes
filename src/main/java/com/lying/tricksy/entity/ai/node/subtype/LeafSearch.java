package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.handler.BlockSearchHandler;
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
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjEntity;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.utility.Region;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LeafSearch implements ISubtypeGroup<LeafNode>
{
	public static final Identifier VARIANT_GET_ITEMS = ISubtypeGroup.variant("get_items");
	public static final Identifier VARIANT_GET_ENTITIES = ISubtypeGroup.variant("get_creatures");
	public static final Identifier VARIANT_GET_INVENTORIES = ISubtypeGroup.variant("get_inventories");
	public static final Identifier VARIANT_GET_MINEABLE = ISubtypeGroup.variant("get_minables");
	public static final Identifier VARIANT_GET_REPLACEABLE = ISubtypeGroup.variant("get_replaceables");
	public static final Identifier VARIANT_GET_MATCHES = ISubtypeGroup.variant("get_matches");
	
	public Identifier getRegistryName() { return new Identifier(Reference.ModInfo.MOD_ID, "leaf_search"); }
	
	public Collection<NodeSubType<LeafNode>> getSubtypes()
	{
		List<NodeSubType<LeafNode>> set = Lists.newArrayList();
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
		add(set, VARIANT_GET_INVENTORIES, new BlockSearchHandler((world, pos, state) -> state.hasBlockEntity() && world.getBlockEntity(pos) instanceof Inventory));
		add(set, VARIANT_GET_MINEABLE, new BlockSearchHandler((world, pos, state) -> state.getHardness(world, pos) >= 0 && !state.getCollisionShape(world, pos).isEmpty()));
		add(set, VARIANT_GET_REPLACEABLE, new BlockSearchHandler((world, pos, state) -> state.isReplaceable()));
		add(set, VARIANT_GET_MATCHES, new BlockSearchHandler()
		{
			private static final WhiteboardRef MATCH = new WhiteboardRef("ref", TFObjType.BLOCK).displayName(CommonVariables.translate("item_filter"));
			private IWhiteboardObject<?> filter = null;
			
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
			{
				super.addVariables(set);
				set.put(MATCH, INodeInput.makeInput(ref -> ref.type() == TFObjType.BLOCK || ref.type() == TFObjType.ITEM, new WhiteboardObj.Item(new ItemStack(Blocks.STONE))));
			}
			
			public boolean test(World world, BlockPos pos, BlockState state)
			{
				if(filter == null || filter.size() == 0)
					return false;
				else if(filter.type() == TFObjType.BLOCK)
					return filter.getAll().stream().anyMatch(block -> world.getBlockState((BlockPos)block).getBlock() == state.getBlock());
				else if(filter.type() == TFObjType.ITEM)
					return filter.getAll().stream().anyMatch(stack -> 
					{
						Item item = ((ItemStack)stack).getItem();
						return item instanceof BlockItem && ((BlockItem)item).getBlock() == state.getBlock();
					});
				return false;
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<BlockPos> getResult(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				filter = getOrDefault(MATCH, parent, local, global);
				return super.getResult(tricksy, local, global, parent);
			}
		});
		return set;
	}
	
	public static List<BlockPos> sortByDistanceTo(BlockPos center, List<BlockPos> positions)
	{
		if(positions.size() > 1)
			positions.sort(new Comparator<BlockPos>()
			{
				public int compare(BlockPos o1, BlockPos o2)
				{
					double dist1 = o1.getSquaredDistance(center);
					double dist2 = o2.getSquaredDistance(center);
					return dist1 < dist2 ? -1 : dist1 > dist2 ? 1 : 0;
				}
			});
		return positions;
	}
}
