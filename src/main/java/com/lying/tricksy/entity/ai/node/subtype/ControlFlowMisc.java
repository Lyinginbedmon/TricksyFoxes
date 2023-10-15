package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.ControlFlowNode;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.node.handler.NodeTickHandler;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ControlFlowMisc implements ISubtypeGroup<ControlFlowNode>
{
	public static final Identifier VARIANT_SEQUENCE = ISubtypeGroup.variant("sequence");
	public static final Identifier VARIANT_SELECTOR = ISubtypeGroup.variant("selector");
	public static final Identifier VARIANT_REACTIVE = ISubtypeGroup.variant("reactive");
	
	public Text displayName() { return Text.translatable("subtype."+Reference.ModInfo.MOD_ID+".control_flow_misc"); }
	
	public Collection<NodeSubType<ControlFlowNode>> getSubtypes()
	{
		List<NodeSubType<ControlFlowNode>> set = Lists.newArrayList();
		set.add(new NodeSubType<ControlFlowNode>(VARIANT_SEQUENCE, new NodeTickHandler<ControlFlowNode>() 
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, ControlFlowNode parent)
			{
				if(!parent.isRunning())
				{
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
			
			public <T extends PathAwareEntity & ITricksyMob<?>> void onEnd(T tricksy, ControlFlowNode parent)
			{
				parent.index = 0;
			}
		}));
		set.add(new NodeSubType<ControlFlowNode>(VARIANT_SELECTOR, new NodeTickHandler<ControlFlowNode>() 
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, ControlFlowNode parent)
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
