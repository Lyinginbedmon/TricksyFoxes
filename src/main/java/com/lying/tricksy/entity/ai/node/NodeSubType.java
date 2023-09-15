package com.lying.tricksy.entity.ai.node;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Global;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Local;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/** Contains the method and data pertaining to a given node subtype */
public class NodeSubType<M extends TreeNode<?>>
{
	private final Identifier registryName;
	private final NodeTickHandler<M> tickFunc;
	
	// TODO Implement necessary whiteboard references
	
	public NodeSubType(Identifier nameIn, NodeTickHandler<M> func)
	{
		this.registryName = nameIn;
		this.tickFunc = func;
	}
	
	public Identifier getRegistryName() { return this.registryName; }
	
	public Text translatedName() { return Text.translatable("variant."+registryName.getNamespace()+"."+registryName.getPath()); }
	
	public Text description() { return Text.translatable("variant."+registryName.getNamespace()+"."+registryName.getPath()+".desc"); }
	
	public <T extends PathAwareEntity & ITricksyMob<?>> Result call(T tricksy, Local<T> local, Global global, M parent)
	{
		// TODO Check required whiteboard values and return FAILURE if they aren't met
		return tickFunc.doTick(tricksy, local, global, parent);
	}
}