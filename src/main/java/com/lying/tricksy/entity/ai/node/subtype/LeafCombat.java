package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.node.handler.CombatHandler;
import com.lying.tricksy.entity.ai.node.handler.INodeInput;
import com.lying.tricksy.entity.ai.node.handler.NodeTickHandler;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.ConstantsWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjEntity;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class LeafCombat implements ISubtypeGroup<LeafNode>
{
	public static final Identifier VARIANT_SET_ATTACK = ISubtypeGroup.variant("set_attack");
	public static final Identifier VARIANT_ATTACK_MELEE = ISubtypeGroup.variant("melee_attack");
	public static final Identifier VARIANT_ATTACK_BOW = ISubtypeGroup.variant("bow_attack");
	public static final Identifier VARIANT_ATTACK_TRIDENT = ISubtypeGroup.variant("trident_attack");
	public static final Identifier VARIANT_ATTACK_CROSSBOW = ISubtypeGroup.variant("crossbow_attack");
	
	public void addActions(Collection<NodeSubType<LeafNode>> set)
	{
		add(set, VARIANT_SET_ATTACK, new NodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.TARGET_ENT, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.ENT), new WhiteboardObjEntity()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				IWhiteboardObject<Entity> value = getOrDefault(CommonVariables.TARGET_ENT, parent, local, global).as(TFObjType.ENT);
				if(value.size() == 0)
				{
					tricksy.setTarget(null);
					return Result.SUCCESS;
				}
				
				Entity ent = value.get();
				if(ent instanceof LivingEntity)
				{
					tricksy.setTarget((LivingEntity)ent);
					return Result.SUCCESS;
				}
				else
					return Result.FAILURE;
			}
		});
		add(set, VARIANT_ATTACK_MELEE, new CombatHandler()
		{
			protected <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result attack(T tricksy, LivingEntity target, LocalWhiteboard<T> local, LeafNode parent)
			{
				if(tricksy.isInAttackRange(target) && !target.isInvulnerable())
				{
					tricksy.swingHand(Hand.MAIN_HAND);
					boolean success = tricksy.tryAttack(target);
					tricksy.logStatus(Text.literal("Have at you!"));
					if(success)
						local.setAttackCooldown(Reference.Values.TICKS_PER_SECOND);
					return success ? Result.SUCCESS : Result.FAILURE;
				}
				return Result.FAILURE;
			}
		});
		add(set, VARIANT_ATTACK_BOW, new RangedCombatHandler()
		{
			private static final WhiteboardRef DRAW = new WhiteboardRef("draw_time", TFObjType.INT).displayName(CommonVariables.translate("draw_time"));
			/** How long the bow should be drawn */
			private int drawTicks = 0;
			
			protected void addVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(DRAW, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.INT), new WhiteboardObj.Int(1), ConstantsWhiteboard.NUM_1.displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				drawTicks = 20 * MathHelper.clamp(getOrDefault(DRAW, parent, local, global).as(TFObjType.INT).get(), 1, 3600);
				return super.doTick(tricksy, local, global, parent);
			}
			
			public boolean isRangeWeapon(ItemStack bowStack) { return !bowStack.isEmpty() && bowStack.getItem() instanceof BowItem; }
			
			public int getDrawTime() { return this.drawTicks; }
			
			protected void attack(LivingEntity target, float pullProgress, PathAwareEntity shooter)
			{
			    ItemStack bowStack = this.getProjectileType(shooter.getMainHandStack(), shooter);
			    PersistentProjectileEntity arrow = ProjectileUtil.createArrowProjectile(shooter, bowStack, pullProgress);
			    double d = target.getX() - shooter.getX();
			    double e = target.getBodyY(0.3333333333333333) - arrow.getY();
			    double f = target.getZ() - shooter.getZ();
			    double g = Math.sqrt(d * d + f * f);
			    
			    World world = shooter.getWorld();
			    arrow.setVelocity(d, e + g * (double)0.2f, f, 1.6f, 14 - world.getDifficulty().getId() * 4);
			    if(pullProgress >= 1F)
			    	arrow.setCritical(true);
			    shooter.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0f, 1.0f / (shooter.getRandom().nextFloat() * 0.4f + 0.8f));
			    world.spawnEntity(arrow);
			}
			
		});
		add(set, VARIANT_ATTACK_TRIDENT, new RangedCombatHandler() 
		{
			public boolean isRangeWeapon(ItemStack bowStack) { return bowStack.isOf(Items.TRIDENT); }
			
			public int getDrawTime() { return Reference.Values.TICKS_PER_SECOND; }
			
			protected void attack(LivingEntity target, float pullProgress, PathAwareEntity shooter)
			{
		        TridentEntity tridentEntity = new TridentEntity(shooter.getWorld(), (LivingEntity)shooter, new ItemStack(Items.TRIDENT));
		        double d = target.getX() - shooter.getX();
		        double e = target.getBodyY(0.3333333333333333) - tridentEntity.getY();
		        double f = target.getZ() - shooter.getZ();
		        double g = Math.sqrt(d * d + f * f);
		        tridentEntity.setVelocity(d, e + g * (double)0.2f, f, 1.6f, 14 - shooter.getWorld().getDifficulty().getId() * 4);
		        shooter.playSound(SoundEvents.ENTITY_DROWNED_SHOOT, 1.0f, 1.0f / (shooter.getRandom().nextFloat() * 0.4f + 0.8f));
		        shooter.getWorld().spawnEntity(tridentEntity);
			}
		});
		add(set, VARIANT_ATTACK_CROSSBOW, new RangedCombatHandler()
		{
			protected <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result attack(T tricksy, LivingEntity target, LocalWhiteboard<T> local, LeafNode parent)
			{
				ItemStack bowStack = tricksy.getMainHandStack();
				if(bowStack.isEmpty() || !bowStack.isOf(Items.CROSSBOW))
					return Result.FAILURE;
				
				switch(getStage(bowStack, tricksy.getActiveItem().equals(bowStack)))
				{
					case UNCHARGED:
						System.out.println("Crossbow uncharged, starting to draw");
						tricksy.logStatus(Text.literal("Draw!"));
						tricksy.setCurrentHand(Hand.MAIN_HAND);
						break;
					case CHARGING:
						int pullTime = CrossbowItem.getPullTime(tricksy.getActiveItem());
						System.out.println("Crossbow uncharged, drawing "+tricksy.getItemUseTime()+" / "+pullTime);
						if(tricksy.getItemUseTime() > CrossbowItem.getPullTime(tricksy.getActiveItem()))
						{
							System.out.println("Drawing interrupted, charged="+CrossbowItem.isCharged(tricksy.getMainHandStack())+", pull progress="+((float)tricksy.getItemUseTime() / pullTime));
							tricksy.setStackInHand(Hand.OFF_HAND, new ItemStack(Items.ARROW));
							tricksy.stopUsingItem();
							tricksy.setStackInHand(Hand.MAIN_HAND, bowStack);
							tricksy.setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY);
						}
						break;
					case CHARGED:
						System.out.println("Crossbow charged, firing");
						return super.attack(tricksy, target, local, parent);
				}
				
				return Result.RUNNING;
			}
			
			public boolean isRangeWeapon(ItemStack bowStack) { return bowStack.isOf(Items.CROSSBOW); }
			
			public int getDrawTime() { return 0; }
			
			protected void attack(LivingEntity target, float pullProgress, PathAwareEntity shooter) 
			{
				// TODO Actually shoot crossbow
				
				
				Hand hand = ProjectileUtil.getHandPossiblyHolding(shooter, Items.CROSSBOW);
				ItemStack bowStack = shooter.getStackInHand(hand);
				CrossbowItem.setCharged(bowStack, false);
				shooter.setStackInHand(hand, bowStack);
			}
			
			private Stage getStage(ItemStack crossbow, boolean isUsing)
			{
				if(CrossbowItem.isCharged(crossbow))
					return Stage.CHARGED;
				else
					return isUsing ? Stage.CHARGING : Stage.UNCHARGED;
			}
			
			enum Stage
			{
				UNCHARGED,
				CHARGING,
				CHARGED;
			}
		});
	}
}
