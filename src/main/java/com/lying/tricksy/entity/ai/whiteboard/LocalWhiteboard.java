package com.lying.tricksy.entity.ai.whiteboard;

import java.util.EnumSet;
import java.util.function.Function;

import com.lying.tricksy.api.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.BehaviourTree.ActionFlag;
import com.lying.tricksy.entity.ai.node.subtype.NodeSubType;
import com.lying.tricksy.entity.ai.whiteboard.object.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.object.WhiteboardObjEntity;
import com.lying.tricksy.init.TFObjType;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;

/** A whiteboard containing locally-accessible values set by a tricksy mob itself */
public class LocalWhiteboard<T extends PathAwareEntity & ITricksyMob<?>> extends Whiteboard<Function<T, IWhiteboardObject<?>>>
{
	public static final WhiteboardRef SELF = makeSystemRef("self", TFObjType.ENT, BoardType.LOCAL);
	public static final WhiteboardRef HP = makeSystemRef("health", TFObjType.INT, BoardType.LOCAL);
	public static final WhiteboardRef ARMOUR = makeSystemRef("armor", TFObjType.INT, BoardType.LOCAL);
	public static final WhiteboardRef USING = makeSystemRef("ticks_using", TFObjType.INT, BoardType.LOCAL);
	public static final WhiteboardRef HANDS_FULL = makeSystemRef("hands_full", TFObjType.BOOL, BoardType.LOCAL);
	public static final WhiteboardRef MAIN_ITEM = makeSystemRef("mainhand_item", TFObjType.ITEM, BoardType.LOCAL);
	public static final WhiteboardRef OFF_ITEM = makeSystemRef("offhand_item", TFObjType.ITEM, BoardType.LOCAL);
	public static final WhiteboardRef HOME = makeSystemRef("home_pos", TFObjType.BLOCK, BoardType.LOCAL);
	public static final WhiteboardRef HAS_SAGE = makeSystemRef("has_sage", TFObjType.BOOL, BoardType.LOCAL);
	public static final WhiteboardRef NEAREST_SAGE = makeSystemRef("nearest_sage", TFObjType.ENT, BoardType.LOCAL);
	public static final WhiteboardRef ATTACK_TARGET = makeSystemRef("attack_target", TFObjType.ENT, BoardType.LOCAL);
	public static final WhiteboardRef ON_GROUND = makeSystemRef("on_ground", TFObjType.BOOL, BoardType.LOCAL);
	
	private final T tricksy;
	
	private int attackCooldown = 0;
	private final ItemCooldownManager itemCooldowns = new ItemCooldownManager();
	private final NodeCooldownManager specialCooldowns = new NodeCooldownManager();
	
	private final EnumSet<ActionFlag> flagsInUse = EnumSet.noneOf(ActionFlag.class);
	
	public LocalWhiteboard(T tricksyIn)
	{
		this(BoardType.LOCAL, tricksyIn);
	}
	
	public LocalWhiteboard(BoardType boardType, T tricksyIn)
	{
		super(boardType, tricksyIn.getWorld());
		tricksy = tricksyIn;
	}
	
	public Whiteboard<?> build()
	{
		register(SELF, (tricksy) -> new WhiteboardObjEntity(tricksy));
		register(HP, (tricksy) -> new WhiteboardObj.Int((int)tricksy.getHealth()));
		register(ARMOUR, (tricksy) -> new WhiteboardObj.Int(tricksy.getArmor()));
		register(HANDS_FULL, (tricksy) -> new WhiteboardObj.Bool(!tricksy.getMainHandStack().isEmpty() && !tricksy.getOffHandStack().isEmpty()));
		register(MAIN_ITEM, (tricksy) -> new WhiteboardObj.Item(tricksy.getMainHandStack()));
		register(OFF_ITEM, (tricksy) -> new WhiteboardObj.Item(tricksy.getOffHandStack()));
		register(USING, (tricksy) -> new WhiteboardObj.Int(tricksy.getItemUseTime()));
		register(HOME, (tricksy) -> tricksy.hasPositionTarget() ? new WhiteboardObjBlock(tricksy.getPositionTarget(), Direction.UP) : TFObjType.BLOCK.blank());
		register(HAS_SAGE, (tricksy) -> new WhiteboardObj.Bool(tricksy.hasSage()));
		register(NEAREST_SAGE, (tricksy) -> 
		{
			PlayerEntity nearestSage = tricksy.getEntityWorld().getClosestPlayer(tricksy.getX(), tricksy.getY(), tricksy.getZ(), 32D, (player) -> tricksy.isSage((PlayerEntity)player));
			return nearestSage == null ? TFObjType.ENT.blank() : new WhiteboardObjEntity(nearestSage);
		});
		register(ATTACK_TARGET, (tricksy) -> tricksy.getAttacking() == null ? TFObjType.ENT.blank() : new WhiteboardObjEntity(tricksy.getAttacking()));
		register(ON_GROUND, (tricksy) -> new WhiteboardObj.Bool(tricksy.isOnGround()));
		return this;
	}
	
	public Whiteboard<Function<T, IWhiteboardObject<?>>> copy()
	{
		LocalWhiteboard<T> copy = new LocalWhiteboard<T>(this.tricksy);
		copy.readFromNbt(writeToNbt(new NbtCompound()));
		return copy;
	}
	
	protected IWhiteboardObject<?> supplierToValue(Function<T, IWhiteboardObject<?>> supplier) { return supplier.apply(tricksy); }
	
	public Function<T, IWhiteboardObject<?>> objectToSupplier(IWhiteboardObject<?> object) { return (tricksy) -> object; }
	
	public void flagAction(EnumSet<ActionFlag> flags)
	{
		for(ActionFlag flag : flags)
			if(!flagsInUse.contains(flag))
				flagsInUse.add(flag);
	}
	
	/** Returns true if the given flag is not already in use by a node in the behaviour tree */
	public boolean canUseFlag(ActionFlag flag) { return !flagsInUse.contains(flag); }
	
	/** Called each tick just before the behaviour tree updates to update cooldowns and refresh action flags */
	public void tick()
	{
		if(attackCooldown > 0)
			attackCooldown--;
		itemCooldowns.update();
		specialCooldowns.update();
		flagsInUse.clear();
	}
	
	public boolean canAttack() { return attackCooldown == 0; }
	
	public void setAttackCooldown(int var) { this.attackCooldown = Math.max(0, var); }
	
	public void setItemCooldown(net.minecraft.item.Item item, int var)
	{
		if(item == Items.AIR)
			return;
		
		this.itemCooldowns.set(item, var);
	}
	
	public boolean isItemCoolingDown(net.minecraft.item.Item item) { return item == Items.AIR ? false : this.itemCooldowns.isCoolingDown(item); }
	
	public void setNodeCooldown(NodeSubType<?> type, int duration) { this.specialCooldowns.putOnCooldown(type, duration); }
	
	public boolean isNodeCoolingDown(NodeSubType<?> type) { return this.specialCooldowns.isOnCooldown(type); }
}