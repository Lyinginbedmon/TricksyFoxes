package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.api.entity.ai.INodeIO;
import com.lying.tricksy.api.entity.ai.INodeTickHandler;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
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
	private final INodeTickHandler<M> tickFunc;
	
	public NodeSubType(Identifier nameIn, INodeTickHandler<M> func)
	{
		this.registryName = nameIn;
		this.tickFunc = func;
	}
	
	public Identifier getRegistryName() { return this.registryName; }
	
	public Text translatedName() { return Text.translatable("variant."+registryName.getNamespace()+"."+registryName.getPath()); }
	
	public Text description() { return Text.translatable("variant."+registryName.getNamespace()+"."+registryName.getPath()+".desc"); }
	
	public Map<WhiteboardRef, INodeIO> inputSet(){ return tickFunc.ioSet(); }
	
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
	public INodeIO getIOCondition(WhiteboardRef reference)
	{
		return tickFunc.ioCondition(reference);
	}
}