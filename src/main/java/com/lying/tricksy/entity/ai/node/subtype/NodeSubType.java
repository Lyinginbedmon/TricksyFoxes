package com.lying.tricksy.entity.ai.node.subtype;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.api.entity.ai.INodeIO;
import com.lying.tricksy.api.entity.ai.INodeIOValue;
import com.lying.tricksy.api.entity.ai.INodeTickHandler;
import com.lying.tricksy.entity.ai.BehaviourTree.ActionFlag;
import com.lying.tricksy.entity.ai.node.NodeType;
import com.lying.tricksy.entity.ai.node.TreeNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardManager;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFNodeStatus;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;

/** Contains the method and data pertaining to a given node subtype */
public class NodeSubType<M extends TreeNode<?>>
{
	private final NodeType<M> parentType;
	private final Identifier registryName;
	private final INodeTickHandler<M> tickFunc;
	private final IntProvider cooldown;
	
	public NodeSubType(Identifier nameIn, @NotNull NodeType<M> parent, INodeTickHandler<M> func)
	{
		this(nameIn, parent, func, ConstantIntProvider.create(0));
	}
	
	public NodeSubType(Identifier nameIn, @NotNull NodeType<M> parent, INodeTickHandler<M> func, IntProvider cooldownIn)
	{
		if(parent == null)
			throw new NullPointerException("Failed to create subtype instance of "+nameIn.toString());
		this.parentType = parent;
		this.registryName = nameIn;
		this.tickFunc = func;
		this.cooldown = cooldownIn;
	}
	
	@SuppressWarnings("unchecked")
	public final TreeNode<M> create(Map<WhiteboardRef, INodeIOValue> ios)
	{
		return (TreeNode<M>)parentType.create(getRegistryName(), ios);
	}
	
	@SuppressWarnings("unchecked")
	public final TreeNode<M> create()
	{
		return (TreeNode<M>)parentType.create(getRegistryName());
	}
	
	public Identifier getRegistryName() { return this.registryName; }
	
	public boolean isValidFor(EntityType<?> typeIn) { return true; }
	
	public Text translatedName() { return Text.translatable("variant."+registryName.getNamespace()+"."+registryName.getPath()); }
	
	public MutableText description() { return Text.translatable("variant."+registryName.getNamespace()+"."+registryName.getPath()+".desc"); }
	
	public List<MutableText> fullDescription() { return List.of(description()); }
	
	/** Returns a value of 0 or greater, reflecting how many ticks before this type of node can be used again */
	public <T extends PathAwareEntity & ITricksyMob<?>> int getCooldown(T tricksy) { return this.cooldown.get(tricksy.getRandom()); }
	
	public EnumSet<ActionFlag> usesFlags() { return tickFunc.flagsUsed(); }
	
	public Map<WhiteboardRef, INodeIO> ioSet(){ return tickFunc.ioSet(); }
	
	public <T extends PathAwareEntity & ITricksyMob<?>> Result call(T tricksy, WhiteboardManager<T> whiteboards, M parent)
	{
		if(!tickFunc.iosSufficient(parent))
			return Result.FAILURE;
		else if(!isValidFor(tricksy.getType()))
		{
			parent.logStatus(TFNodeStatus.INVALID_USER);
			return Result.FAILURE;
		}
		
		return tickFunc.doTick(tricksy, whiteboards, parent);
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