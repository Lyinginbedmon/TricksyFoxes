package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.api.entity.ai.INodeTickHandler;
import com.lying.tricksy.entity.ai.node.ControlFlowNode;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Identifier;

public class ControlFlowMisc implements ISubtypeGroup<ControlFlowNode>
{
	public static final Identifier VARIANT_SEQUENCE = ISubtypeGroup.variant("sequence");
	public static final Identifier VARIANT_SELECTOR = ISubtypeGroup.variant("selector");
	public static final Identifier VARIANT_REACTIVE = ISubtypeGroup.variant("reactive");
	
	public Identifier getRegistryName() { return new Identifier(Reference.ModInfo.MOD_ID, "control_flow_misc"); }
	
	public Collection<NodeSubType<ControlFlowNode>> getSubtypes()
	{
		List<NodeSubType<ControlFlowNode>> set = Lists.newArrayList();
		set.add(new NodeSubType<ControlFlowNode>(VARIANT_SEQUENCE, new INodeTickHandler<ControlFlowNode>() 
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, ControlFlowNode parent)
			{
				if(!parent.children().isEmpty())
				{
					TreeNode<?> child = parent.children().get(parent.index % parent.children().size());
					switch(child.tick(tricksy, local, global))
					{
						case FAILURE:
							return Result.FAILURE;
						case SUCCESS:
							if(++parent.index == parent.children().size())
								return Result.SUCCESS;
						case RUNNING:
						default:
							return Result.RUNNING;
					}
				}
				return Result.SUCCESS;
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> void onEnd(T tricksy, ControlFlowNode parent)
			{
				parent.index = 0;
			}
		}));
		set.add(new NodeSubType<ControlFlowNode>(VARIANT_SELECTOR, new INodeTickHandler<ControlFlowNode>() 
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, ControlFlowNode parent)
			{
				if(parent.children().isEmpty())
					return Result.FAILURE;
				
				if(!parent.isRunning())
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
		}));
		set.add(new NodeSubType<ControlFlowNode>(VARIANT_REACTIVE, new INodeTickHandler<ControlFlowNode>() 
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, ControlFlowNode parent)
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
					parent.children().forEach((child) -> { if(child.isRunning()) child.stop(tricksy); });
				
				return result;
			}
		}));
		return set;
	}
}
