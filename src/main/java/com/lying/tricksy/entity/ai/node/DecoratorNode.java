package com.lying.tricksy.entity.ai.node;

import java.util.Collection;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.Whiteboard.Global;
import com.lying.tricksy.entity.ai.Whiteboard.Local;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

/**
 * TODO NODE TYPES
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
	public static final Identifier VARIANT_DELAY_1S = new Identifier(Reference.ModInfo.MOD_ID, "delay_1s");
	public static final Identifier VARIANT_DELAY_5S = new Identifier(Reference.ModInfo.MOD_ID, "delay_5s");
	public static final Identifier VARIANT_DELAY_10S = new Identifier(Reference.ModInfo.MOD_ID, "delay_10s");
	
	protected int ticks = 20;
	
	protected DecoratorNode(UUID uuidIn)
	{
		super(TFNodeTypes.DECORATOR, uuidIn);
	}
	
	public final boolean canAddChild(TreeNode<?> child) { return children().isEmpty(); }
	
	public final boolean isRunnable() { return children().size() == 1; }
	
	public final TreeNode<?> child() { return children().isEmpty() ? null : children().get(0); }
	
	public static DecoratorNode delay(UUID uuidIn)
	{
		return (DecoratorNode)TFNodeTypes.DECORATOR.create(uuidIn, new NbtCompound()).setSubType(VARIANT_DELAY_1S);
	}
	
	public static DecoratorNode inverter(UUID uuidIn)
	{
		return (DecoratorNode)TFNodeTypes.DECORATOR.create(uuidIn, new NbtCompound()).setSubType(VARIANT_INVERTER);
	}
	
	public static DecoratorNode fromData(UUID uuidIn, NbtCompound data)
	{
		return new DecoratorNode(uuidIn);
	}
	
	private static NodeTickHandler<DecoratorNode> makeDelay(int duration)
	{
		return new NodeTickHandler<DecoratorNode>()
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, DecoratorNode parent)
			{
				DecoratorNode decorator = (DecoratorNode)parent;
				if(decorator.ticks-- <= 0)
				{
					decorator.ticks = duration * Reference.Values.TICKS_PER_SECOND;
					return decorator.child().tick(tricksy, local, global);
				}
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
		set.add(new NodeSubType<DecoratorNode>(VARIANT_DELAY_1S, makeDelay(1)));
		set.add(new NodeSubType<DecoratorNode>(VARIANT_DELAY_5S, makeDelay(5)));
		set.add(new NodeSubType<DecoratorNode>(VARIANT_DELAY_10S, makeDelay(10)));
	}
}
