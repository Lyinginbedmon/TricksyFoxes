package com.lying.tricksy.entity.ai.node;

import java.util.Collection;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Global;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Local;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class ConditionNode extends TreeNode<ConditionNode>
{
	public static final Identifier VARIANT_HAS_MASTER = new Identifier(Reference.ModInfo.MOD_ID, "has_master");
	public static final Identifier VARIANT_MASTER_NEARBY = new Identifier(Reference.ModInfo.MOD_ID, "master_nearby");
	public static final Identifier VARIANT_MASTER_PRESENT = new Identifier(Reference.ModInfo.MOD_ID, "master_present");
	
	public ConditionNode(UUID uuidIn)
	{
		super(TFNodeTypes.CONDITION, uuidIn);
	}
	
	public static ConditionNode fromData(UUID uuid, NbtCompound data)
	{
		return new ConditionNode(uuid);
	}
	
	public static ConditionNode hasMaster(UUID uuid) { return (ConditionNode)TFNodeTypes.CONDITION.create(uuid, new NbtCompound()).setSubType(VARIANT_HAS_MASTER); }
	public static ConditionNode masterPresent(UUID uuid) { return (ConditionNode)TFNodeTypes.CONDITION.create(uuid, new NbtCompound()).setSubType(VARIANT_MASTER_PRESENT); }
	public static ConditionNode masterNearby(UUID uuid) { return (ConditionNode)TFNodeTypes.CONDITION.create(uuid, new NbtCompound()).setSubType(VARIANT_MASTER_NEARBY); }
	
	public static void populateSubTypes(Collection<NodeSubType<ConditionNode>> set)
	{
		set.add(new NodeSubType<ConditionNode>(VARIANT_HAS_MASTER, new NodeTickHandler<ConditionNode>()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, ConditionNode parent)
			{
				return tricksy.hasMaster() ? Result.SUCCESS : Result.FAILURE;
			}
		}));
		set.add(new NodeSubType<ConditionNode>(VARIANT_MASTER_NEARBY, new NodeTickHandler<ConditionNode>()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, ConditionNode parent)
			{
				if(!tricksy.hasMaster())
					return Result.FAILURE;
				IWhiteboardObject<?> master = Whiteboard.get(Whiteboard.Local.NEAREST_MASTER, local, global);
				if(master.isEmpty())
					return Result.FAILURE;
				return master.as(TFObjType.ENT).get().distanceTo(tricksy) < 16D ? Result.SUCCESS : Result.FAILURE;
			}
		}));
		set.add(new NodeSubType<ConditionNode>(VARIANT_MASTER_PRESENT, new NodeTickHandler<ConditionNode>()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, ConditionNode parent)
			{
				if(!tricksy.hasMaster())
					return Result.FAILURE;
				IWhiteboardObject<?> master = Whiteboard.get(Whiteboard.Local.NEAREST_MASTER, local, global);
				return master.isEmpty() ? Result.FAILURE : Result.SUCCESS;
			}
		}));
	}
}
