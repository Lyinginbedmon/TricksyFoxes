package com.lying.tricksy.entity.ai.node.subtype;

import java.util.List;
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
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
	
	public boolean isValidFor(EntityType<?> typeIn) { return true; }
	
	public Text translatedName() { return Text.translatable("variant."+registryName.getNamespace()+"."+registryName.getPath()); }
	
	public MutableText description() { return Text.translatable("variant."+registryName.getNamespace()+"."+registryName.getPath()+".desc"); }
	
	public List<MutableText> fullDescription() { return List.of(description()); }
	
	public <T extends PathAwareEntity & ITricksyMob<?>> int cooldown(T tricksy) { return tickFunc.getCooldown(tricksy); }
	
	public Map<WhiteboardRef, INodeIO> inputSet(){ return tickFunc.ioSet(); }
	
	public <T extends PathAwareEntity & ITricksyMob<?>> Result call(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, M parent)
	{
		if(!tickFunc.inputsSufficient(parent) || !isValidFor(tricksy.getType()))
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
	
	public static MutableText exclusivityDesc(Text descIn)
	{
		return Text.translatable("info."+Reference.ModInfo.MOD_ID+".node_exclusivity", descIn).styled(style -> style.withBold(true).withColor(Formatting.GOLD));
	}
	
	public static MutableText cooldownDesc(Text descIn)
	{
		return Text.translatable("info."+Reference.ModInfo.MOD_ID+".node_cooldown", descIn).styled(style -> style.withColor(Formatting.GRAY));
	}
}