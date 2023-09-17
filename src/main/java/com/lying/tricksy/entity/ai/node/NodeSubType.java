package com.lying.tricksy.entity.ai.node;

import java.util.Map;
import java.util.function.Predicate;

import com.lying.tricksy.TricksyFoxes;
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Global;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Local;
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
	
	public Map<WhiteboardRef, Predicate<WhiteboardRef>> variableSet(){ return tickFunc.variableSet(); }
	
	public <T extends PathAwareEntity & ITricksyMob<?>> Result call(T tricksy, Local<T> local, Global global, M parent)
	{
		if(!tickFunc.variablesSufficient(parent))
		{
			TricksyFoxes.LOGGER.warn("Tree node of type "+parent.getType().getRegistryName().toString()+", subtype: "+registryName.toString()+" in entity "+tricksy.getDisplayName().getString()+" is missing one or more input variables");
			return Result.FAILURE;
		}
		
		return tickFunc.doTick(tricksy, local, global, parent);
	}
}