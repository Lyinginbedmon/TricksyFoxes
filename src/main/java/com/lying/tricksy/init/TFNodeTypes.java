package com.lying.tricksy.init;

import java.util.HashMap;
import java.util.List;
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
	
	public static final NodeType<ControlFlowNode> CONTROL_FLOW = register("control_flow", new NodeType<ControlFlowNode>(12596790, ControlFlowNode::fromData, ControlFlowNode::populateSubTypes).setBaseSubType(ControlFlowNode.VARIANT_SEQUENCE));
	public static final NodeType<DecoratorNode> DECORATOR = register("decorator", new NodeType<DecoratorNode>(3555008, DecoratorNode::fromData, DecoratorNode::populateSubTypes).setBaseSubType(DecoratorNode.VARIANT_INVERTER));
	public static final NodeType<ConditionNode> CONDITION = register("condition", new NodeType<ConditionNode>(12630070, ConditionNode::fromData, ConditionNode::populateSubTypes).setBaseSubType(ConditionNode.VARIANT_VALUE_TRUE));
	public static final NodeType<LeafNode> LEAF = register("leaf", new NodeType<LeafNode>(3588150, LeafNode::fromData, LeafNode::populateSubTypes).setBaseSubType(LeafNode.VARIANT_GOTO));
	
	public static final List<NodeType<?>> NODE_TYPES = List.of(LEAF, CONTROL_FLOW, DECORATOR, CONDITION);
	
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
			type.subTypes().forEach((sub) -> TricksyFoxes.LOGGER.info(" # - "+sub.toString()));
		});
	}
}
