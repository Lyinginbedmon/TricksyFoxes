package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.api.entity.ai.INodeIO;
import com.lying.tricksy.api.entity.ai.INodeTickHandler;
import com.lying.tricksy.entity.ai.node.DecoratorNode;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.node.handler.NodeInput;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Identifier;

public class DecoratorMisc implements ISubtypeGroup<DecoratorNode>
{
	public static final Identifier VARIANT_INVERTER = ISubtypeGroup.variant("inverter");
	public static final Identifier VARIANT_FORCE_FAILURE = ISubtypeGroup.variant("force_failure");
	public static final Identifier VARIANT_FORCE_SUCCESS = ISubtypeGroup.variant("force_success");
	public static final Identifier VARIANT_DELAY = ISubtypeGroup.variant("delay");
	public static final Identifier VARIANT_REPEAT = ISubtypeGroup.variant("repeat");
	public static final Identifier VARIANT_RETRY = ISubtypeGroup.variant("retry");
	public static final Identifier VARIANT_FOR_EACH = ISubtypeGroup.variant("for_each");
	public static final Identifier VARIANT_DO_ONCE = ISubtypeGroup.variant("do_once");
	public static final Identifier VARIANT_WAIT_COOL = ISubtypeGroup.variant("wait_for_cooldown");
	
	public Identifier getRegistryName() { return new Identifier(Reference.ModInfo.MOD_ID, "decorator_misc"); }

	@Override
	public Collection<NodeSubType<DecoratorNode>> getSubtypes()
	{
		List<NodeSubType<DecoratorNode>> set = Lists.newArrayList();
		set.add(new NodeSubType<DecoratorNode>(VARIANT_INVERTER, new INodeTickHandler<DecoratorNode>()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, DecoratorNode parent)
			{
				switch(parent.child().tick(tricksy, local, global))
				{
					case FAILURE:
						return Result.SUCCESS;
					case RUNNING:
						return Result.RUNNING;
					case SUCCESS:
						return Result.FAILURE;
				}
				return Result.FAILURE;
			}
		}));
		set.add(new NodeSubType<DecoratorNode>(VARIANT_FORCE_FAILURE, new INodeTickHandler<DecoratorNode>()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, DecoratorNode parent)
			{
				return parent.child().tick(tricksy, local, global).isEnd() ? Result.FAILURE : Result.RUNNING;
			}
		}));
		set.add(new NodeSubType<DecoratorNode>(VARIANT_FORCE_SUCCESS, new INodeTickHandler<DecoratorNode>()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, DecoratorNode parent)
			{
				return parent.child().tick(tricksy, local, global).isEnd() ? Result.SUCCESS : Result.RUNNING;
			}
		}));
		set.add(new NodeSubType<DecoratorNode>(VARIANT_DELAY, new INodeTickHandler<DecoratorNode>()
		{
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.VAR_NUM, NodeInput.makeInput(NodeInput.any(), new WhiteboardObj.Int(1)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, DecoratorNode parent)
			{
				IWhiteboardObject<Integer> duration = getOrDefault(CommonVariables.VAR_NUM, parent, local, global).as(TFObjType.INT);
				
				if(!parent.isRunning())
					parent.ticks = duration.get() * Reference.Values.TICKS_PER_SECOND;
				else if(parent.ticks-- <= 0)
					return parent.child().tick(tricksy, local, global);
				return Result.RUNNING;
			}
		}));
		set.add(new NodeSubType<DecoratorNode>(VARIANT_FOR_EACH, new INodeTickHandler<DecoratorNode>()
		{
			public static final WhiteboardRef LIST = new WhiteboardRef("value_to_cycle", TFObjType.BOOL).displayName(CommonVariables.translate("to_cycle"));
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(LIST, NodeInput.makeInput(NodeInput.any()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, DecoratorNode parent)
			{
				IWhiteboardObject<?> value = getOrDefault(LIST, parent, local, global);
				if(value.size() == 0)
					return Result.SUCCESS;
				
				if(!parent.isRunning())
					parent.ticks = 0;
				
				if(parent.child().tick(tricksy, local, global).isEnd())
				{
					value.cycle();
					if(++parent.ticks == value.size())
						return Result.SUCCESS;
				}
				return Result.RUNNING;
			}
		}));
		set.add(new NodeSubType<DecoratorNode>(VARIANT_REPEAT, new INodeTickHandler<DecoratorNode>()
		{
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.VAR_NUM, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int(4)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, DecoratorNode parent)
			{
				IWhiteboardObject<Integer> duration = getOrDefault(CommonVariables.VAR_NUM, parent, local, global).as(TFObjType.INT);
				if(!parent.isRunning())
					parent.ticks = 0;
				
				Result result = parent.child().tick(tricksy, local, global);
				if(result == Result.FAILURE)
					return Result.FAILURE;
				
				if(result.isEnd() && ++parent.ticks == duration.get())
					return Result.SUCCESS;
				
				return Result.RUNNING;
			}
		}));
		set.add(new NodeSubType<DecoratorNode>(VARIANT_RETRY, new INodeTickHandler<DecoratorNode>()
		{
			public @NotNull Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.VAR_NUM, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int(4)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, DecoratorNode parent)
			{
				IWhiteboardObject<Integer> duration = getOrDefault(CommonVariables.VAR_NUM, parent, local, global).as(TFObjType.INT);
				if(!parent.isRunning())
					parent.ticks = 0;
				
				Result result = parent.child().tick(tricksy, local, global);
				if(result == Result.SUCCESS)
					return Result.SUCCESS;
				
				if(result.isEnd() && ++parent.ticks == duration.get())
					return Result.FAILURE;
				
				return Result.RUNNING;
			}
		}));
		set.add(new NodeSubType<DecoratorNode>(VARIANT_DO_ONCE, new INodeTickHandler<DecoratorNode>()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, DecoratorNode parent)
			{
				if(parent.ticks > 0)
					return Result.FAILURE;
				
				Result result = parent.child().tick(tricksy, local, global);
				if(result.isEnd())
				{
					parent.ticks = 1;
					return result;
				}
				return Result.RUNNING;
			}
		}));
		set.add(new NodeSubType<DecoratorNode>(VARIANT_WAIT_COOL, new INodeTickHandler<DecoratorNode>()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, DecoratorNode parent)
			{
				if(isCoolingDownRecursive(parent.child(), local))
					return Result.RUNNING;
				
				return parent.child().tick(tricksy, local, global);
			}
			
			private boolean isCoolingDownRecursive(TreeNode<?> node, LocalWhiteboard<?> local)
			{
				if(local.isNodeCoolingDown(node.getSubType()))
					return true;
				
				if(!node.children().isEmpty())
					if(node.children().stream().anyMatch(child -> isCoolingDownRecursive(child, local)))
						return true;
				
				return false;
			}
		}));
		return set;
	}
}
