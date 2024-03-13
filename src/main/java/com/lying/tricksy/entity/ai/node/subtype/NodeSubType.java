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
	
	public EnumSet<ActionFlag> flagsUsed() { return tickFunc.flagsUsed(); }
	
	public final NodePhase getPhase(int ticksRunning)
	{
		if(tickFunc.castingTime() > 0)
			switch((int)Math.signum(ticksRunning - tickFunc.castingTime()))
			{
				case -1:	return NodePhase.CASTING;
				case 0:		return NodePhase.CAST;
				default:
				case 1:		 return NodePhase.TICK;
			}
		else
			return ticksRunning > 0 ? NodePhase.TICK : NodePhase.CAST;
	}
	
	public boolean shouldCooldown(int ticksRunning, NodePhase phase, Result latestResult) { return tickFunc.cooldownBehaviour().apply(ticksRunning, phase, latestResult); }
	
	public boolean breaksOnDamage() { return this.tickFunc.shouldBreakOnDamage(); }
	
	public Map<WhiteboardRef, INodeIO> ioSet(){ return tickFunc.ioSet(); }
	
	public final <T extends PathAwareEntity & ITricksyMob<?>> Result call(T tricksy, WhiteboardManager<T> whiteboards, M parent)
	{
		if(!tickFunc.iosSufficient(parent))
			return Result.FAILURE;
		else if(!isValidFor(tricksy.getType()))
		{
			parent.logStatus(TFNodeStatus.INVALID_USER, Text.literal("Can't use this node"));
			return Result.FAILURE;
		}
		
		if(!parent.isRunning() && !tickFunc.validityCheck(tricksy, whiteboards, parent))
			return Result.FAILURE;
		else
			switch(getPhase(parent.ticksRunning()))
			{
				case CASTING:
					Result result = tickFunc.doCasting(tricksy, whiteboards, parent, parent.ticksRunning());
					if(!result.isEnd())
						parent.logStatus(TFNodeStatus.CASTING);
					return result;
				case CAST:
					return tickFunc.onCast(tricksy, whiteboards, parent);
				case TICK:
				default:
					return tickFunc.onTick(tricksy, whiteboards, parent, parent.ticksRunning() - tickFunc.castingTime());
			}
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
	
	public static enum CooldownBehaviour
	{
		ALWAYS((ticks,phase,result) -> true),
		IF_NOT_IMMEDIATE_FAILURE((ticks,phase,result) -> !(result == Result.FAILURE && ticks == 0)),
		IF_ACTIVE((ticks,phase,result) -> phase.active()),
		IF_SUCCESS((ticks,phase,result) -> result == Result.SUCCESS);
		
		private final CooldownFunc func;
		
		private CooldownBehaviour(CooldownFunc funcIn)
		{
			this.func = funcIn;
		}
		
		public boolean apply(int ticks, NodePhase phase, Result result) { return func.apply(ticks, phase, result); }
		
		@FunctionalInterface
		public interface CooldownFunc
		{
			public boolean apply(int ticks, NodePhase phase, Result result);
		}
	}
	
	public static enum NodePhase
	{
		CASTING(false),
		CAST(true),
		TICK(true);
		
		private final boolean isActive;
		
		private NodePhase(boolean active)
		{
			isActive = active;
		}
		
		public boolean active() { return this.isActive; }
	}
}