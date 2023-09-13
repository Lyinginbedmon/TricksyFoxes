package com.lying.tricksy.init;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.TricksyFoxes;
import com.lying.tricksy.entity.ai.node.ConditionNode;
import com.lying.tricksy.entity.ai.node.ControlFlowNode;
import com.lying.tricksy.entity.ai.node.DecoratorNode;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.NodeType;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.reference.Reference;

import net.minecraft.util.Identifier;

public class TFNodeTypes
{
	/**
	 * NODE TYPES<br>
	 * Nodes are divided by a NodeType which contains a subset of variants to select from.<br>
	 * Variants may have additional options to choose, as well as different input requirements.<br>
	 * 
	 * Control Flow	- Executes child nodes in particular ways<br>
	 * Condition	- Monitors values and returns success or failure based on them, may accept object references<br>
	 * Decorator	- Alters the result or modifies the operation of a singular child node<br>
	 * Leaf	- Performs an action and has no child nodes<br>
	 */
	private static final Map<Identifier, NodeType<?>> TYPES = new HashMap<>();
	
	public static final NodeType<ControlFlowNode> CONTROL_FLOW = register("control_flow", new NodeType<ControlFlowNode>(ControlFlowNode::fromData, ControlFlowNode::populateSubTypes));
	public static final NodeType<DecoratorNode> DECORATOR = register("decorator", new NodeType<DecoratorNode>(DecoratorNode::fromData, DecoratorNode::populateSubTypes));
	public static final NodeType<ConditionNode> CONDITION = register("condition", new NodeType<ConditionNode>(ConditionNode::fromData, ConditionNode::populateSubTypes));
	public static final NodeType<LeafNode> LEAF = register("leaf", new NodeType<LeafNode>(LeafNode::fromData, LeafNode::populateSubTypes));
	
	private static <M extends TreeNode<M>> NodeType<M> register(String nameIn, NodeType<M> typeIn)
	{
		Identifier registryName = new Identifier(Reference.ModInfo.MOD_ID, nameIn);
		typeIn.setRegistryName(registryName);
		TYPES.put(registryName, typeIn);
		return typeIn;
	}
	
	@Nullable
	public static NodeType<?> getTypeById(Identifier idIn)
	{
		return TYPES.getOrDefault(idIn, null);
	}
	
	public static void init()
	{
		TricksyFoxes.LOGGER.info("Registered "+TYPES.size()+" behaviour tree node types");
		int tally = 0;
		for(NodeType<?> type : TYPES.values())
			tally += Math.max(1, type.subTypes().size());
		TricksyFoxes.LOGGER.info(" "+tally+" available node behaviours");
		
		TYPES.forEach((name,type) -> 
		{
			TricksyFoxes.LOGGER.info(" # "+name.toString());
			type.subTypes().forEach((sub) -> TricksyFoxes.LOGGER.info(" # - "+sub.getRegistryName().toString()));
		});
	}
}
