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
import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjEntity;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Global;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Local;
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
	public static final Identifier VARIANT_GET_ITEM = ISubtypeGroup.variant("nearest_item");
	public static final Identifier VARIANT_GET_MONSTER = ISubtypeGroup.variant("nearest_hostile");
	public static final Identifier VARIANT_GET_ASSAILANT = ISubtypeGroup.variant("get_assailant");
	
	public void addActions(Collection<NodeSubType<LeafNode>> set)
	{
		set.add(new NodeSubType<LeafNode>(VARIANT_GET_ITEM, new GetterHandler<Entity>(TFObjType.ENT)
		{
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.VAR_POS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK), new WhiteboardObjBlock(), Local.SELF.displayName())); 
				set.put(CommonVariables.VAR_DIS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.INT), new WhiteboardObj.Int((int)NodeTickHandler.INTERACT_RANGE)));
				set.put(CommonVariables.VAR_ITEM, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.ITEM), new WhiteboardObj.Item()));
			}

			@Override
			public <N extends PathAwareEntity & ITricksyMob<?>> @Nullable IWhiteboardObject<Entity> getResult(N tricksy, Local<N> local, Global global, LeafNode parent)
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
				
				List<ItemEntity> items = world.getEntitiesByClass(ItemEntity.class, search, (item) -> InventoryHandler.matchesFilter(item.getStack(), filter));
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
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_GET_MONSTER, new GetterHandler<Entity>(TFObjType.ENT)
		{
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.VAR_POS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.BLOCK), new WhiteboardObjBlock(), Local.SELF.displayName())); 
				set.put(CommonVariables.VAR_DIS, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.INT), new WhiteboardObj.Int((int)NodeTickHandler.INTERACT_RANGE)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Entity> getResult(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				IWhiteboardObject<Integer> range = getOrDefault(CommonVariables.VAR_DIS, parent, local, global).as(TFObjType.INT);
				IWhiteboardObject<BlockPos> pos = getOrDefault(CommonVariables.VAR_POS, parent, local, global).as(TFObjType.BLOCK);
				
				int searchRange = MathHelper.clamp(range.get(), 0, 16);
				BlockPos point = pos.size() == 0 ? tricksy.getBlockPos() : pos.get();
				Box search = new Box(point).expand(searchRange);
				
				World world = tricksy.getWorld();
				if(search.minY < world.getBottomY())
					search = search.withMinY(world.getBottomY());
				
				List<MobEntity> monsters = world.getEntitiesByClass(MobEntity.class, search, (ent) -> ent.isAlive() && ent instanceof Monster && ent != tricksy);
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
		}));
		set.add(new NodeSubType<LeafNode>(VARIANT_GET_ASSAILANT, new GetterHandler<Entity>(TFObjType.ENT)
		{
			public void addVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(CommonVariables.TARGET_ENT, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.ENT), new WhiteboardObjEntity(), Local.SELF.displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> IWhiteboardObject<Entity> getResult(T tricksy, Local<T> local, Global global, LeafNode parent)
			{
				IWhiteboardObject<Entity> target = getOrDefault(CommonVariables.TARGET_ENT, parent, local, global).as(TFObjType.ENT);
				
				Entity entity = target.size() == 0 ? tricksy : target.get();
				if(!(entity instanceof LivingEntity))
					return null;
				
				Entity assailant = ((LivingEntity)entity).getAttacker();
				return assailant == null || assailant == tricksy ? new WhiteboardObjEntity() : new WhiteboardObjEntity(assailant);
			}
		}));
	}
}
