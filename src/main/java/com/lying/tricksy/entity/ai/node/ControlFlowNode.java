package com.lying.tricksy.entity.ai.node;

import java.util.Collection;
import java.util.UUID;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Global;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Local;
import com.lying.tricksy.init.TFNodeTypes;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

/**
 * TODO NODE TYPES
 * Control Flow	- Executes child nodes in particular ways
 * 		Selector	- Executes the first node that does not return failure
 * 		Sequential	- Executes each node one after the other until end or one returns failure
 * 		Reactive	- Executes all nodes until any return failure or all return success
 */
public class ControlFlowNode extends TreeNode<ControlFlowNode>
{
	public static final Identifier VARIANT_SEQUENCE = new Identifier(Reference.ModInfo.MOD_ID, "sequence");
	public static final Identifier VARIANT_SELECTOR = new Identifier(Reference.ModInfo.MOD_ID, "selector");
	public static final Identifier VARIANT_REACTIVE = new Identifier(Reference.ModInfo.MOD_ID, "reactive");
	
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
				if(!parent.children().isEmpty())
				{
					TreeNode<?> child = parent.children().get(parent.index % parent.children().size());
					switch(child.tick(tricksy, local, global))
					{
						case FAILURE:
							parent.index = 0;
							return Result.FAILURE;
						case SUCCESS:
							if(++parent.index >= parent.children().size())
							{
								parent.index = 0;
								return Result.SUCCESS;
							}
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
				if(!parent.children().isEmpty())
				{
					if(parent.isRunning())
					{
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
						return Result.FAILURE;
					}
					else
					{
						// Tick indexed node
						TreeNode<?> child = parent.children().get(parent.index);
						return child.tick(tricksy, local, global);
					}
				}
				
				return Result.SUCCESS;
			}
		}));
	}
}
