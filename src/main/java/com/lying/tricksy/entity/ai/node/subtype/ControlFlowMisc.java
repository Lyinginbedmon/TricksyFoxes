package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.api.entity.ai.INodeTickHandler;
import com.lying.tricksy.entity.ai.node.ControlFlowNode;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardManager;
import com.lying.tricksy.init.TFNodeStatus;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Identifier;

public class ControlFlowMisc extends NodeGroupControlFlow
{
	public static final Identifier VARIANT_SEQUENCE = ISubtypeGroup.variant("sequence");
	
	public static NodeSubType<ControlFlowNode> SEQUENCE;
	public static NodeSubType<ControlFlowNode> SELECTOR;
	public static NodeSubType<ControlFlowNode> REACTIVE;
	
	public Identifier getRegistryName() { return new Identifier(Reference.ModInfo.MOD_ID, "control_flow_misc"); }
	
	public Collection<NodeSubType<ControlFlowNode>> getSubtypes()
	{
		List<NodeSubType<ControlFlowNode>> set = Lists.newArrayList();
		set.add(SEQUENCE = subtype(VARIANT_SEQUENCE, new ControlFlowHandler() 
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> Result onTick(T tricksy, WhiteboardManager<T> whiteboards, ControlFlowNode parent, int tick)
			{
				TreeNode<?> child = parent.children().get(parent.index % parent.children().size());
				switch(child.tick(tricksy, whiteboards))
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
			
			public <T extends PathAwareEntity & ITricksyMob<?>> void onEnd(T tricksy, ControlFlowNode parent)
			{
				parent.index = 0;
			}
		}));
		set.add(SELECTOR = subtype(ISubtypeGroup.variant("selector"), new ControlFlowHandler() 
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> Result onCast(T tricksy, WhiteboardManager<T> whiteboards, ControlFlowNode parent)
			{
				// Find first viable child node
				for(int i=0; i<parent.children().size(); i++)
				{
					TreeNode<?> child = parent.children().get(i);
					Result result = child.tick(tricksy, whiteboards);
					if(result != Result.FAILURE)
					{
						parent.index = i;
						return result;
					}
				}
				return Result.FAILURE;
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> Result onTick(T tricksy, WhiteboardManager<T> whiteboards, ControlFlowNode parent, int tick)
			{
				// Tick indexed node
				return parent.children().get(parent.index).tick(tricksy, whiteboards);
			}
		}));
		set.add(REACTIVE = subtype(ISubtypeGroup.variant("reactive"), new ControlFlowHandler() 
		{
			public <T extends PathAwareEntity & ITricksyMob<?>> Result onTick(T tricksy, WhiteboardManager<T> whiteboards, ControlFlowNode parent, int tick)
			{
				/**
				 * Parent result is equal to:
				 * 	FAILURE = Any child node failed
				 * 	RUNNING = No nodes have failed but some are still in progress
				 * 	SUCCESS = All nodes succeeded
				 */
				Result result = Result.SUCCESS;
				for(TreeNode<?> child : parent.children())
					switch(child.tick(tricksy, whiteboards))
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
					parent.children().forEach((child) -> { if(child.isRunning()) child.stop(tricksy, whiteboards); });
				
				return result;
			}
		}));
		return set;
	}
	
	private abstract class ControlFlowHandler implements INodeTickHandler<ControlFlowNode>
	{
		public boolean iosSufficient(ControlFlowNode parent)
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
