package com.lying.tricksy.init;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.TricksyFoxes;
import com.lying.tricksy.entity.ai.node.ConditionNode;
import com.lying.tricksy.entity.ai.node.ControlFlowNode;
import com.lying.tricksy.entity.ai.node.DecoratorNode;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.NodeType;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.node.subtype.ConditionWhiteboard;
import com.lying.tricksy.entity.ai.node.subtype.ControlFlowMisc;
import com.lying.tricksy.entity.ai.node.subtype.DecoratorMisc;
import com.lying.tricksy.entity.ai.node.subtype.LeafMisc;
import com.lying.tricksy.reference.Reference;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class TFNodeTypes
{
	public static final Identifier ROSE_FLOWER = new Identifier(Reference.ModInfo.MOD_ID, "textures/gui/tree/branch_rose.png");
	public static final Identifier GRAPE_FLOWER = new Identifier(Reference.ModInfo.MOD_ID, "textures/gui/tree/branch_grapes.png");
	
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
	
	public static final NodeType<ControlFlowNode> CONTROL_FLOW = register("control_flow", new NodeType<ControlFlowNode>(12596790, ROSE_FLOWER, ControlFlowNode::fromData, ControlFlowNode::getSubtypeGroups).setBaseSubType(ControlFlowMisc.VARIANT_SEQUENCE));
	public static final NodeType<DecoratorNode> DECORATOR = register("decorator", new NodeType<DecoratorNode>(3555008, GRAPE_FLOWER, DecoratorNode::fromData, DecoratorNode::getSubtypeGroups).setBaseSubType(DecoratorMisc.VARIANT_INVERTER));
	public static final NodeType<ConditionNode> CONDITION = register("condition", new NodeType<ConditionNode>(12630070, ConditionNode::fromData, ConditionNode::getSubtypeGroups).setBaseSubType(ConditionWhiteboard.VARIANT_VALUE_TRUE));
	public static final NodeType<LeafNode> LEAF = register("leaf", new NodeType<LeafNode>(3588150, LeafNode::fromData, LeafNode::getSubtypeGroups).setBaseSubType(LeafMisc.VARIANT_GOTO));
	
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
		return TFRegistries.TYPE_REGISTRY.get(idIn);
	}
	
	public static void init()
	{
		TYPES.forEach((name, type) -> Registry.register(TFRegistries.TYPE_REGISTRY, name, type));
		
		int tally = 0;
		for(Entry<RegistryKey<NodeType<?>>, NodeType<?>> entry : TFRegistries.TYPE_REGISTRY.getEntrySet())
			tally += entry.getValue().subTypes().size();
		TricksyFoxes.LOGGER.info("Registered "+TYPES.size()+" behaviour tree node types with "+tally+" available behaviours");
		
		if(TricksyFoxes.config.verboseLogging())
			TFRegistries.TYPE_REGISTRY.getEntrySet().forEach((entry) -> 
			{
				Identifier name = entry.getKey().getValue();
				NodeType<?> type = entry.getValue();
				TricksyFoxes.LOGGER.info(" # "+name.toString()+" ("+type.subTypes().size()+" subtypes in "+type.groups().size()+" groups)");
				type.groups().forEach((group) -> 
				{
					TricksyFoxes.LOGGER.info(" # # "+group.getRegistryName().getPath());
					group.getSubtypes().forEach((sub) -> TricksyFoxes.LOGGER.info(" # # - "+sub.getRegistryName().getPath()));
				});
			});
	}
}
