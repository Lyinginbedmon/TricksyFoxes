package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.api.entity.ai.INodeIO;
import com.lying.tricksy.api.entity.ai.INodeTickHandler;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.handler.BlockSearchHandler;
import com.lying.tricksy.entity.ai.node.handler.GetterHandlerTyped;
import com.lying.tricksy.entity.ai.node.handler.InventoryHandler;
import com.lying.tricksy.entity.ai.node.handler.MatchBlockSearchHandler;
import com.lying.tricksy.entity.ai.node.handler.NodeInput;
import com.lying.tricksy.entity.ai.node.subtype.NodeSubType.CooldownBehaviour;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.ConstantsWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardManager;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
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
import net.minecraft.world.World;

public class LeafSearch extends NodeGroupLeaf
{
	public static NodeSubType<LeafNode> GET_ITEMS;
	public static NodeSubType<LeafNode> GET_ENTITIES;
	public static NodeSubType<LeafNode> GET_INVENTORIES;
	public static NodeSubType<LeafNode> GET_MINEABLE;
	public static NodeSubType<LeafNode> GET_REPLACEABLE;
	public static NodeSubType<LeafNode> GET_MATCHES;
	
	public Identifier getRegistryName() { return new Identifier(Reference.ModInfo.MOD_ID, "leaf_search"); }
	
	public Collection<NodeSubType<LeafNode>> getSubtypes()
	{
		List<NodeSubType<LeafNode>> set = Lists.newArrayList();
		set.add(GET_ITEMS = subtype(ISubtypeGroup.variant("get_items"), new GetterHandlerTyped<Entity>(TFObjType.ENT)
		{
			public CooldownBehaviour cooldownBehaviour() { return CooldownBehaviour.ALWAYS; }
			
			public void addInputVariables(Map<WhiteboardRef, INodeIO> set)
			{
				set.put(CommonVariables.VAR_POS, GetterHandlerTyped.POS_OR_REGION);
				set.put(CommonVariables.VAR_DIS, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, false), new WhiteboardObj.Int((int)INodeTickHandler.INTERACT_RANGE)));
				set.put(CommonVariables.VAR_ITEM, NodeInput.makeInput(NodeInput.ofType(TFObjType.ITEM, true), new WhiteboardObj.Item()));
			}
			
			public <N extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Entity> getTypedResult(N tricksy, WhiteboardManager<N> whiteboards, LeafNode parent)
			{
				IWhiteboardObject<?> pos = getOrDefault(CommonVariables.VAR_POS, parent, whiteboards);
				IWhiteboardObject<Integer> range = getOrDefault(CommonVariables.VAR_DIS, parent, whiteboards).as(TFObjType.INT);
				IWhiteboardObject<ItemStack> filter = getOrDefault(CommonVariables.VAR_ITEM, parent, whiteboards).as(TFObjType.ITEM);
				
				Region searchArea = GetterHandlerTyped.getSearchArea(pos, range, tricksy);
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
		}, Reference.Values.TICKS_PER_SECOND));
		set.add(GET_ENTITIES = subtype(ISubtypeGroup.variant("get_creatures"), new GetterHandlerTyped<Entity>(TFObjType.ENT)
		{
			public static WhiteboardRef FILTER = new WhiteboardRef("entity_filter", TFObjType.ENT).displayName(CommonVariables.translate("item_filter"));
			
			public CooldownBehaviour cooldownBehaviour() { return CooldownBehaviour.ALWAYS; }
			
			public void addInputVariables(Map<WhiteboardRef, INodeIO> set)
			{
				set.put(CommonVariables.VAR_POS, GetterHandlerTyped.POS_OR_REGION);
				set.put(CommonVariables.VAR_DIS, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, false), new WhiteboardObj.Int((int)INodeTickHandler.INTERACT_RANGE)));
				set.put(FILTER, NodeInput.makeInput(NodeInput.ofType(TFObjType.ENT, true), ConstantsWhiteboard.FILTER_MONSTER.copy(), ConstantsWhiteboard.ENT_MONSTERS.displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Entity> getTypedResult(T tricksy, WhiteboardManager<T> whiteboards, LeafNode parent)
			{
				IWhiteboardObject<?> pos = getOrDefault(CommonVariables.VAR_POS, parent, whiteboards);
				IWhiteboardObject<Integer> range = getOrDefault(CommonVariables.VAR_DIS, parent, whiteboards).as(TFObjType.INT);
				IWhiteboardObject<Entity> filter = getOrDefault(FILTER, parent, whiteboards).as(TFObjType.ENT);
				
				Region searchArea = GetterHandlerTyped.getSearchArea(pos, range, tricksy);
				World world = tricksy.getWorld();
				List<LivingEntity> mobs = searchArea.getEntitiesByClass(LivingEntity.class, world, (ent) -> INodeTickHandler.matchesEntityFilter(ent, filter));
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
		}, Reference.Values.TICKS_PER_SECOND));
		set.add(GET_INVENTORIES = subtype(ISubtypeGroup.variant("get_inventories"), new BlockSearchHandler((world, pos, state) -> state.hasBlockEntity() && world.getBlockEntity(pos) instanceof Inventory), Reference.Values.TICKS_PER_SECOND));
		set.add(GET_MINEABLE = subtype(ISubtypeGroup.variant("get_minables"), new BlockSearchHandler((world, pos, state) -> state.getHardness(world, pos) >= 0 && !state.getCollisionShape(world, pos).isEmpty()), Reference.Values.TICKS_PER_SECOND));
		set.add(GET_REPLACEABLE = subtype(ISubtypeGroup.variant("get_replaceables"), new BlockSearchHandler((world, pos, state) -> state.isReplaceable()), Reference.Values.TICKS_PER_SECOND));
		set.add(GET_MATCHES = subtype(ISubtypeGroup.variant("get_matches"), new MatchBlockSearchHandler(), Reference.Values.TICKS_PER_SECOND));
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
