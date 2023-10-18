package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.DecoratorNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.node.handler.INodeInput;
import com.lying.tricksy.entity.ai.node.handler.NodeTickHandler;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.text.Text;
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
	
	public Identifier getRegistryName() { return new Identifier(Reference.ModInfo.MOD_ID, "decorator_misc"); }

	@Override
	public Collection<NodeSubType<DecoratorNode>> getSubtypes()
	{
		List<NodeSubType<DecoratorNode>> set = Lists.newArrayList();
		set.add(new NodeSubType<DecoratorNode>(VARIANT_INVERTER, new NodeTickHandler<DecoratorNode>()
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
		set.add(new NodeSubType<DecoratorNode>(VARIANT_FORCE_FAILURE, new NodeTickHandler<DecoratorNode>()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, DecoratorNode parent)
			{
				return parent.child().tick(tricksy, local, global).isEnd() ? Result.FAILURE : Result.RUNNING;
			}
		}));
		set.add(new NodeSubType<DecoratorNode>(VARIANT_FORCE_SUCCESS, new NodeTickHandler<DecoratorNode>()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, DecoratorNode parent)
			{
				return parent.child().tick(tricksy, local, global).isEnd() ? Result.SUCCESS : Result.RUNNING;
			}
		}));
		set.add(new NodeSubType<DecoratorNode>(VARIANT_DELAY, new NodeTickHandler<DecoratorNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.VAR_NUM, INodeInput.makeInput(NodeTickHandler.any(), new WhiteboardObj.Int(1)));
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
		set.add(new NodeSubType<DecoratorNode>(VARIANT_FOR_EACH, new NodeTickHandler<DecoratorNode>()
		{
			public static final WhiteboardRef LIST = new WhiteboardRef("value_to_cycle", TFObjType.BOOL).displayName(CommonVariables.translate("to_cycle"));
			
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(LIST, INodeInput.makeInput(NodeTickHandler.any()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, DecoratorNode parent)
			{
				IWhiteboardObject<?> value = getOrDefault(LIST, parent, local, global);
				if(!parent.isRunning())
				{
					tricksy.logStatus(Text.literal("Running "+parent.child().getSubType().translatedName().getString()+" "+value.size()+" times"));
					parent.ticks = 0;
				}
				
				if(parent.child().tick(tricksy, local, global).isEnd())
				{
					value.cycle();
					if(++parent.ticks == value.size())
						return Result.SUCCESS;
				}
				return Result.RUNNING;
			}
		}));
		set.add(new NodeSubType<DecoratorNode>(VARIANT_REPEAT, new NodeTickHandler<DecoratorNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.VAR_NUM, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.INT, true), new WhiteboardObj.Int(4)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, DecoratorNode parent)
			{
				IWhiteboardObject<Integer> duration = getOrDefault(CommonVariables.VAR_NUM, parent, local, global).as(TFObjType.INT);
				if(!parent.isRunning())
				{
					tricksy.logStatus(Text.literal("Repeating "+parent.child().getSubType().translatedName().getString()+" "+duration.get()+" times"));
					parent.ticks = 0;
				}
				
				Result result = parent.child().tick(tricksy, local, global);
				if(result == Result.FAILURE)
					return Result.FAILURE;
				
				if(result.isEnd() && ++parent.ticks == duration.get())
					return Result.SUCCESS;
				
				return Result.RUNNING;
			}
		}));
		set.add(new NodeSubType<DecoratorNode>(VARIANT_RETRY, new NodeTickHandler<DecoratorNode>()
		{
			public @NotNull Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.VAR_NUM, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.INT, true), new WhiteboardObj.Int(4)));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, DecoratorNode parent)
			{
				IWhiteboardObject<Integer> duration = getOrDefault(CommonVariables.VAR_NUM, parent, local, global).as(TFObjType.INT);
				if(!parent.isRunning())
				{
					tricksy.logStatus(Text.literal("Retrying "+parent.child().getSubType().translatedName().getString()+" "+duration.get()+" times"));
					parent.ticks = 0;
				}
				
				Result result = parent.child().tick(tricksy, local, global);
				if(result == Result.SUCCESS)
					return Result.SUCCESS;
				
				if(result.isEnd() && ++parent.ticks == duration.get())
					return Result.FAILURE;
				
				return Result.RUNNING;
			}
		}));
		set.add(new NodeSubType<DecoratorNode>(VARIANT_DO_ONCE, new NodeTickHandler<DecoratorNode>()
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
		return set;
	}
}