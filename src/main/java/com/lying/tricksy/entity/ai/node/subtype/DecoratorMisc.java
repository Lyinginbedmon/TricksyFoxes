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
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardManager;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.init.TFNodeStatus;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Identifier;

public class DecoratorMisc extends NodeGroupDecorator
{
	public static final Identifier VARIANT_INVERTER = ISubtypeGroup.variant("inverter");
	
	public static NodeSubType<DecoratorNode> INVERTER;
	public static NodeSubType<DecoratorNode> FORCE_FAILURE;
	public static NodeSubType<DecoratorNode> FORCE_SUCCESS;
	public static NodeSubType<DecoratorNode> DELAY;
	public static NodeSubType<DecoratorNode> FOR_EACH;
	public static NodeSubType<DecoratorNode> REPEAT;
	public static NodeSubType<DecoratorNode> RETRY;
	public static NodeSubType<DecoratorNode> DO_ONCE;
	public static NodeSubType<DecoratorNode> WAIT_FOR_COOLDOWN;
	
	public Identifier getRegistryName() { return new Identifier(Reference.ModInfo.MOD_ID, "decorator_misc"); }
	
	public Collection<NodeSubType<DecoratorNode>> getSubtypes()
	{
		List<NodeSubType<DecoratorNode>> set = Lists.newArrayList();
		set.add(INVERTER = subtype(VARIANT_INVERTER, new DecoratorHandler()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onTick(T tricksy, WhiteboardManager<T> whiteboards, DecoratorNode parent)
			{
				switch(parent.child().tick(tricksy, whiteboards))
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
		set.add(FORCE_FAILURE = subtype(ISubtypeGroup.variant("force_failure"), new DecoratorHandler()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onTick(T tricksy, WhiteboardManager<T> whiteboards, DecoratorNode parent)
			{
				return parent.child().tick(tricksy, whiteboards).isEnd() ? Result.FAILURE : Result.RUNNING;
			}
		}));
		set.add(FORCE_SUCCESS = subtype(ISubtypeGroup.variant("force_success"), new DecoratorHandler()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onTick(T tricksy, WhiteboardManager<T> whiteboards, DecoratorNode parent)
			{
				return parent.child().tick(tricksy, whiteboards).isEnd() ? Result.SUCCESS : Result.RUNNING;
			}
		}));
		set.add(DELAY = subtype(ISubtypeGroup.variant("delay"), new DecoratorHandler()
		{
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.VAR_NUM, NodeInput.makeInput(NodeInput.any(), new WhiteboardObj.Int(1)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onTick(T tricksy, WhiteboardManager<T> whiteboards, DecoratorNode parent)
			{
				IWhiteboardObject<Integer> duration = getOrDefault(CommonVariables.VAR_NUM, parent, whiteboards).as(TFObjType.INT);
				
				if(!parent.isRunning())
					parent.ticks = duration.get() * Reference.Values.TICKS_PER_SECOND;
				else if(parent.ticks-- <= 0)
					return parent.child().tick(tricksy, whiteboards);
				return Result.RUNNING;
			}
		}));
		set.add(FOR_EACH = subtype(ISubtypeGroup.variant("for_each"), new DecoratorHandler()
		{
			public static final WhiteboardRef LIST = new WhiteboardRef("value_to_cycle", TFObjType.BOOL).displayName(CommonVariables.translate("to_cycle"));
			
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(LIST, NodeInput.makeInput(NodeInput.any()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onTick(T tricksy, WhiteboardManager<T> whiteboards, DecoratorNode parent)
			{
				IWhiteboardObject<?> value = getOrDefault(LIST, parent, whiteboards);
				if(value.size() == 0)
					return Result.SUCCESS;
				
				if(!parent.isRunning())
					parent.ticks = 0;
				
				switch(parent.child().tick(tricksy, whiteboards))
				{
					case FAILURE:
						return Result.FAILURE;
					case SUCCESS:
						value.cycle();
						if(++parent.ticks == value.size())
							return Result.SUCCESS;
					default:
					case RUNNING:
						return Result.RUNNING;
				}
			}
		}));
		set.add(REPEAT = subtype(ISubtypeGroup.variant("repeat"), new DecoratorHandler()
		{
			public Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.VAR_NUM, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int(4)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onTick(T tricksy, WhiteboardManager<T> whiteboards, DecoratorNode parent)
			{
				IWhiteboardObject<Integer> duration = getOrDefault(CommonVariables.VAR_NUM, parent, whiteboards).as(TFObjType.INT);
				if(!parent.isRunning())
					parent.ticks = 0;
				
				Result result = parent.child().tick(tricksy, whiteboards);
				if(result == Result.FAILURE)
					return Result.FAILURE;
				
				if(result.isEnd() && ++parent.ticks == duration.get())
					return Result.SUCCESS;
				
				return Result.RUNNING;
			}
		}));
		set.add(RETRY = subtype(ISubtypeGroup.variant("retry"), new DecoratorHandler()
		{
			public @NotNull Map<WhiteboardRef, INodeIO> ioSet()
			{
				return Map.of(CommonVariables.VAR_NUM, NodeInput.makeInput(NodeInput.ofType(TFObjType.INT, true), new WhiteboardObj.Int(4)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onTick(T tricksy, WhiteboardManager<T> whiteboards, DecoratorNode parent)
			{
				IWhiteboardObject<Integer> duration = getOrDefault(CommonVariables.VAR_NUM, parent, whiteboards).as(TFObjType.INT);
				if(!parent.isRunning())
					parent.ticks = 0;
				
				Result result = parent.child().tick(tricksy, whiteboards);
				if(result == Result.SUCCESS)
					return Result.SUCCESS;
				
				if(result.isEnd() && ++parent.ticks == duration.get())
					return Result.FAILURE;
				
				return Result.RUNNING;
			}
		}));
		set.add(DO_ONCE = subtype(ISubtypeGroup.variant("do_once"), new DecoratorHandler()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onTick(T tricksy, WhiteboardManager<T> whiteboards, DecoratorNode parent)
			{
				if(parent.ticks > 0)
					return Result.FAILURE;
				
				Result result = parent.child().tick(tricksy, whiteboards);
				if(result.isEnd())
				{
					parent.ticks = 1;
					return result;
				}
				return Result.RUNNING;
			}
		}));
		set.add(WAIT_FOR_COOLDOWN = subtype(ISubtypeGroup.variant("wait_for_cooldown"), new DecoratorHandler()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result onTick(T tricksy, WhiteboardManager<T> whiteboards, DecoratorNode parent)
			{
				if(isCoolingDownRecursive(parent.child(), whiteboards.local()))
					return Result.RUNNING;
				
				return parent.child().tick(tricksy, whiteboards);
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
	
	private abstract class DecoratorHandler implements INodeTickHandler<DecoratorNode>
	{
		public boolean iosSufficient(DecoratorNode parent)
		{
			if(!parent.hasChildren())
			{
				parent.logStatus(TFNodeStatus.NO_CHILDREN);
				return false;
			}
			return INodeTickHandler.super.iosSufficient(parent);
		}
	}
}
