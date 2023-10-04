package com.lying.tricksy.entity.ai.node;

import java.util.Collection;
import java.util.UUID;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.handler.NodeTickHandler;
import com.lying.tricksy.entity.ai.node.subtype.ISubtypeGroup;
import com.lying.tricksy.entity.ai.node.subtype.NodeSubType;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Global;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Local;
import com.lying.tricksy.init.TFNodeTypes;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * NODE TYPES
 * Control Flow	- Executes child nodes in particular ways
 * 		Selector	- Executes the first node that does not return failure
 * 		Sequential	- Executes each node one after the other until end or one returns failure
 * 		Reactive	- Executes all nodes until any return failure or all return success
 */
public class ControlFlowNode extends TreeNode<ControlFlowNode>
{
	public static final Identifier VARIANT_SEQUENCE = ISubtypeGroup.variant("sequence");
	public static final Identifier VARIANT_SELECTOR = ISubtypeGroup.variant("selector");
	public static final Identifier VARIANT_REACTIVE = ISubtypeGroup.variant("reactive");
	
	private int index = 0;
	
	public ControlFlowNode(UUID uuidIn)
	{
		super(TFNodeTypes.CONTROL_FLOW, uuidIn);
	}
	
	public static ControlFlowNode fromData(UUID uuidIn, NbtCompound data) { return new ControlFlowNode(uuidIn); }
	
	public static void populateSubTypes(Collection<NodeSubType<ControlFlowNode>> set)
	{
		set.add(new NodeSubType<ControlFlowNode>(VARIANT_SEQUENCE, new NodeTickHandler<ControlFlowNode>() 
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> Result doTick(T tricksy, Local<T> local, Global global, ControlFlowNode parent)
			{
				if(!parent.isRunning())
				{
					parent.index = 0;
					tricksy.logStatus(Text.literal("Starting sequence of "+parent.children().size()+" steps"));
					tricksy.logStatus(Text.literal("Step 1: ").append(parent.children().get(0).getSubType().translatedName().getString()));
				}
				
				if(!parent.children().isEmpty())
				{
					TreeNode<?> child = parent.children().get(parent.index % parent.children().size());
					switch(child.tick(tricksy, local, global))
					{
						case FAILURE:
							tricksy.logStatus(Text.literal("Step "+(parent.index + 1)+" failed!"));
							return Result.FAILURE;
						case SUCCESS:
							if(++parent.index == parent.children().size())
							{
								tricksy.logStatus(Text.literal("Sequence completed!"));
								return Result.SUCCESS;
							}
							else
								tricksy.logStatus(Text.literal("Step "+(parent.index + 1)+": ").append(parent.children().get(parent.index).getSubType().translatedName().getString()));
						case RUNNING:
						default:
							return Result.RUNNING;
					}
				}
				
				return Result.SUCCESS;
			}
		}));
		set.add(new NodeSubType<ControlFlowNode>(VARIANT_SELECTOR, new NodeTickHandler<ControlFlowNode>() 
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> Result doTick(T tricksy, Local<T> local, Global global, ControlFlowNode parent)
			{
				if(parent.children().isEmpty())
					return Result.FAILURE;
				
				if(!parent.isRunning())
				{
					tricksy.logStatus(Text.literal("Picking best option from selection"));
					// Find first viable child node
					for(int i=0; i<parent.children().size(); i++)
					{
						TreeNode<?> child = parent.children().get(i);
						Result result = child.tick(tricksy, local, global);
						if(result != Result.FAILURE)
						{
							parent.index = i;
							return result;
						}
					}
					tricksy.logStatus(Text.literal("I don't know what to do!"));
					return Result.FAILURE;
				}
				else
				{
					// Tick indexed node
					TreeNode<?> child = parent.children().get(parent.index);
					return child.tick(tricksy, local, global);
				}
			}
		}));
		set.add(new NodeSubType<ControlFlowNode>(VARIANT_REACTIVE, new NodeTickHandler<ControlFlowNode>() 
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> Result doTick(T tricksy, Local<T> local, Global global, ControlFlowNode parent)
			{
				/**
				 * Parent result is equal to:
				 * 	FAILURE = Any child node failed
				 * 	RUNNING = No nodes have failed but some are still in progress
				 * 	SUCCESS = All nodes succeeded
				 */
				Result result = Result.SUCCESS;
				for(TreeNode<?> child : parent.children())
					switch(child.tick(tricksy, local, global))
					{
						case FAILURE:
							result = Result.FAILURE;
							break;
						case RUNNING:
							if(result != Result.FAILURE)
								result = Result.RUNNING;
							break;
						case SUCCESS:
							break;
					}
				
				if(result.isEnd())
					parent.children().forEach((child) -> { if(child.isRunning()) child.stop(); });
				
				return result;
			}
		}));
	}
}
