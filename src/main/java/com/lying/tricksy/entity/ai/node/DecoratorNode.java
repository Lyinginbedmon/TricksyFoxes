package com.lying.tricksy.entity.ai.node;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Global;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Local;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

/**
 * NODE TYPES
 * Decorator	- Alters the result or modifies the operation of a singular child node
 * 		Force failure	- Always returns failure
 * 		Force success	- Always returns success
 * 		Inverter		- Returns the opposite of the child node (failure = success, success = failure, running unchanged)
 * 		Repeat			- Runs the child N times or until it fails
 * 		Retry			- Runs the child N times or until it succeeds
 * 		Delay			- Runs the child after N ticks
 */
public class DecoratorNode extends TreeNode<DecoratorNode>
{
	public static final Identifier VARIANT_INVERTER = new Identifier(Reference.ModInfo.MOD_ID, "inverter");
	public static final Identifier VARIANT_FORCE_FAILURE = new Identifier(Reference.ModInfo.MOD_ID, "force_failure");
	public static final Identifier VARIANT_FORCE_SUCCESS = new Identifier(Reference.ModInfo.MOD_ID, "force_success");
	public static final Identifier VARIANT_DELAY = new Identifier(Reference.ModInfo.MOD_ID, "delay");
	public static final Identifier VARIANT_REPEAT = new Identifier(Reference.ModInfo.MOD_ID, "repeat");
	public static final Identifier VARIANT_RETRY = new Identifier(Reference.ModInfo.MOD_ID, "retry");
	
	protected int ticks = 20;
	
	protected DecoratorNode(UUID uuidIn)
	{
		super(TFNodeTypes.DECORATOR, uuidIn);
	}
	
	public final boolean canAddChild() { return children().isEmpty(); }
	
	public final boolean isRunnable() { return children().size() == 1; }
	
	public final TreeNode<?> child() { return children().isEmpty() ? null : children().get(0); }
	
	public static DecoratorNode inverter(UUID uuidIn)
	{
		return (DecoratorNode)TFNodeTypes.DECORATOR.create(uuidIn, new NbtCompound()).setSubType(VARIANT_INVERTER);
	}
	
	public static DecoratorNode fromData(UUID uuidIn, NbtCompound data)
	{
		return new DecoratorNode(uuidIn);
	}
	
	public static NodeTickHandler<DecoratorNode> makeDelay(int duration)
	{
		return new NodeTickHandler<DecoratorNode>()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, DecoratorNode parent)
			{
				if(!parent.isRunning())
					parent.ticks = duration * Reference.Values.TICKS_PER_SECOND;
				else if(parent.ticks-- <= 0)
					return parent.child().tick(tricksy, local, global);
				return Result.RUNNING;
			}
		};
	}
	
	public static NodeTickHandler<DecoratorNode> makeRepeater(int tally)
	{
		return new NodeTickHandler<DecoratorNode>()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, DecoratorNode parent)
			{
				if(!parent.isRunning())
					parent.ticks = tally;
				
				Result result = parent.child().tick(tricksy, local, global);
				if(result == Result.FAILURE)
					return Result.FAILURE;
				else if(--parent.ticks == 0)
					return Result.SUCCESS;
				return Result.RUNNING;
			}
		};
	}
	
	public static NodeTickHandler<DecoratorNode> makeRetry(int tally)
	{
		return new NodeTickHandler<DecoratorNode>()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, DecoratorNode parent)
			{
				if(!parent.isRunning())
					parent.ticks = tally;
				
				Result result = parent.child().tick(tricksy, local, global);
				if(result == Result.SUCCESS)
					return Result.SUCCESS;
				else if(--parent.ticks == 0)
					return Result.FAILURE;
				return Result.RUNNING;
			}
		};
	}
	
	public static void populateSubTypes(Collection<NodeSubType<DecoratorNode>> set)
	{
		set.add(new NodeSubType<DecoratorNode>(VARIANT_INVERTER, new NodeTickHandler<DecoratorNode>()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, DecoratorNode parent)
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
		set.add(new NodeSubType<DecoratorNode>(VARIANT_FORCE_FAILURE, new NodeTickHandler<DecoratorNode>()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, DecoratorNode parent)
			{
				return parent.child().tick(tricksy, local, global).isEnd() ? Result.FAILURE : Result.RUNNING;
			}
		}));
		set.add(new NodeSubType<DecoratorNode>(VARIANT_FORCE_SUCCESS, new NodeTickHandler<DecoratorNode>()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, DecoratorNode parent)
			{
				return parent.child().tick(tricksy, local, global).isEnd() ? Result.SUCCESS : Result.RUNNING;
			}
		}));
		set.add(new NodeSubType<DecoratorNode>(VARIANT_DELAY, new NodeTickHandler<DecoratorNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.VAR_COUNT, INodeInput.makeInput(NodeTickHandler.any(), new WhiteboardObj.Int(1)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, DecoratorNode parent)
			{
				IWhiteboardObject<Integer> duration = getOrDefault(CommonVariables.VAR_COUNT, parent, local, global).as(TFObjType.INT);
				
				if(!parent.isRunning())
					parent.ticks = duration.get() * Reference.Values.TICKS_PER_SECOND;
				else if(parent.ticks-- <= 0)
					return parent.child().tick(tricksy, local, global);
				return Result.RUNNING;
			}
		}));
		set.add(new NodeSubType<DecoratorNode>(VARIANT_REPEAT, new NodeTickHandler<DecoratorNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.VAR_COUNT, INodeInput.makeInput(NodeTickHandler.any(), new WhiteboardObj.Int(4)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, DecoratorNode parent)
			{
				if(!parent.isRunning())
					parent.ticks = 0;
				
				Result result = parent.child().tick(tricksy, local, global);
				if(result == Result.FAILURE)
					return Result.FAILURE;
				
				IWhiteboardObject<Integer> duration = getOrDefault(CommonVariables.VAR_COUNT, parent, local, global).as(TFObjType.INT);
				if(result.isEnd() && parent.ticks++ >= duration.get())
					return Result.SUCCESS;
				
				return Result.RUNNING;
			}
		}));
		set.add(new NodeSubType<DecoratorNode>(VARIANT_RETRY, new NodeTickHandler<DecoratorNode>()
		{
			public @NotNull Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.VAR_COUNT, INodeInput.makeInput(NodeTickHandler.any(), new WhiteboardObj.Int(4)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, DecoratorNode parent)
			{
				if(!parent.isRunning())
					parent.ticks = 0;
				
				Result result = parent.child().tick(tricksy, local, global);
				if(result == Result.SUCCESS)
					return Result.SUCCESS;
				
				IWhiteboardObject<Integer> duration = getOrDefault(CommonVariables.VAR_COUNT, parent, local, global).as(TFObjType.INT);
				if(result.isEnd() && parent.ticks++ >= duration.get())
					return Result.FAILURE;
				
				return Result.RUNNING;
			}
		}));
	}
}
