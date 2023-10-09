package com.lying.tricksy.entity.ai.whiteboard;

import java.util.function.Function;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.init.TFObjType;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Direction;

/** A whiteboard containing locally-accessible values set by a tricksy mob itself */
public class LocalWhiteboard<T extends PathAwareEntity & ITricksyMob<?>> extends Whiteboard<Function<T, IWhiteboardObject<?>>>
{
	public static final WhiteboardRef SELF = makeSystemRef("self", TFObjType.ENT, BoardType.LOCAL);
	public static final WhiteboardRef HP = makeSystemRef("health", TFObjType.INT, BoardType.LOCAL);
	public static final WhiteboardRef ARMOUR = makeSystemRef("armor", TFObjType.INT, BoardType.LOCAL);
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
	private ItemCooldownManager itemCooldowns = new ItemCooldownManager();
	
	public LocalWhiteboard(T tricksyIn)
	{
		super(BoardType.LOCAL, tricksyIn.getWorld());
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
	
	protected IWhiteboardObject<?> supplierToValue(Function<T, IWhiteboardObject<?>> supplier) { return supplier.apply(tricksy); }
	
	public Function<T, IWhiteboardObject<?>> objectToSupplier(IWhiteboardObject<?> object) { return (tricksy) -> object; }
	
	public void tick()
	{
		if(attackCooldown > 0)
			attackCooldown = Math.max(0, attackCooldown - 1);
		itemCooldowns.update();
	}
	
	public boolean canAttack() { return attackCooldown == 0; }
	
	public void setAttackCooldown(int var) { this.attackCooldown = Math.max(0, var); }
	
	public void setItemCooldown(net.minecraft.item.Item item, int var)
	{
		if(item == Items.AIR)
			return;
		
		this.itemCooldowns.set(item, var);
	}
	
	public boolean isCoolingDown(net.minecraft.item.Item item) { return item == Items.AIR ? false : this.itemCooldowns.isCoolingDown(item); }
}