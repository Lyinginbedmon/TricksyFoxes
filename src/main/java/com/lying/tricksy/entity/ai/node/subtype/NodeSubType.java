package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.node.handler.INodeInput;
import com.lying.tricksy.entity.ai.node.handler.NodeTickHandler;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/** Contains the method and data pertaining to a given node subtype */
public class NodeSubType<M extends TreeNode<?>>
{
	private final Identifier registryName;
	private final NodeTickHandler<M> tickFunc;
	
	public NodeSubType(Identifier nameIn, NodeTickHandler<M> func)
	{
		this.registryName = nameIn;
		this.tickFunc = func;
	}
	
	public Identifier getRegistryName() { return this.registryName; }
	
	public Text translatedName() { return Text.translatable("variant."+registryName.getNamespace()+"."+registryName.getPath()); }
	
	public Text description() { return Text.translatable("variant."+registryName.getNamespace()+"."+registryName.getPath()+".desc"); }
	
	public Map<WhiteboardRef, INodeInput> inputSet(){ return tickFunc.inputSet(); }
	
	public <T extends PathAwareEntity & ITricksyMob<?>> Result call(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, M parent)
	{
		if(!tickFunc.inputsSufficient(parent))
			return Result.FAILURE;
		
		return tickFunc.doTick(tricksy, local, global, parent);
	}
	
	/** Performs any end-of-behaviour cleanup */
	public <T extends PathAwareEntity & ITricksyMob<?>> void onEnd(T tricksy, M parent)
	{
		tickFunc.onEnd(tricksy, parent);
	}
	
	@Nullable
	public INodeInput getInputCondition(WhiteboardRef reference)
	{
		return tickFunc.inputCondition(reference);
	}
}