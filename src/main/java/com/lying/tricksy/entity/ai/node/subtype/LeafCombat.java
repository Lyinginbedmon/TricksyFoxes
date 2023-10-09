package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.node.handler.CombatHandler;
import com.lying.tricksy.entity.ai.node.handler.INodeInput;
import com.lying.tricksy.entity.ai.node.handler.NodeTickHandler;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Global;
import com.lying.tricksy.entity.ai.whiteboard.Whiteboard.Local;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjEntity;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class LeafCombat implements ISubtypeGroup<LeafNode>
{
	public static final Identifier VARIANT_SET_ATTACK = ISubtypeGroup.variant("set_attack");
	public static final Identifier VARIANT_ATTACK_MELEE = ISubtypeGroup.variant("melee_attack");
	public static final Identifier VARIANT_ATTACK_BOW = ISubtypeGroup.variant("bow_attack");
	
	public void addActions(Collection<NodeSubType<LeafNode>> set)
	{
		add(set, VARIANT_SET_ATTACK, new NodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.TARGET_ENT, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.ENT), new WhiteboardObjEntity()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, Local<T> local, Global global, LeafNode parent)
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
			protected <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result attack(T tricksy, LivingEntity target, Local<T> local, LeafNode parent)
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
		add(set, VARIANT_ATTACK_BOW, new CombatHandler()
		{
			protected <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result attack(T tricksy, LivingEntity target, Local<T> local, LeafNode parent)
			{
				ItemStack bowStack = tricksy.getMainHandStack();
				if(bowStack.isEmpty() || bowStack.getItem() != Items.BOW)
					return Result.FAILURE;
				
				if(!parent.isRunning())
				{
					tricksy.setCurrentHand(Hand.MAIN_HAND);
					return Result.RUNNING;
				}
				else if(!target.isInvulnerable() && tricksy.getItemUseTime() >= 20)
				{
					tricksy.logStatus(Text.literal("Firing!"));
					attack(target, 1F, tricksy);
					tricksy.clearActiveItem();
					local.setAttackCooldown(Reference.Values.TICKS_PER_SECOND);
					return Result.SUCCESS;
				}
				return Result.FAILURE;
			}
			
		    public void attack(LivingEntity target, float pullProgress, PathAwareEntity shooter)
		    {
		        ItemStack bowStack = this.getProjectileType(shooter.getMainHandStack(), shooter);
		        PersistentProjectileEntity arrow = ProjectileUtil.createArrowProjectile(shooter, bowStack, pullProgress);
		        double d = target.getX() - shooter.getX();
		        double e = target.getBodyY(0.3333333333333333) - arrow.getY();
		        double f = target.getZ() - shooter.getZ();
		        double g = Math.sqrt(d * d + f * f);
		        
		        World world = shooter.getWorld();
		        arrow.setVelocity(d, e + g * (double)0.2f, f, 1.6f, 14 - world.getDifficulty().getId() * 4);
		        shooter.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0f, 1.0f / (shooter.getRandom().nextFloat() * 0.4f + 0.8f));
		        world.spawnEntity(arrow);
		    }
		    
		    public ItemStack getProjectileType(ItemStack stack, PathAwareEntity shooter)
		    {
		        if (stack.getItem() instanceof RangedWeaponItem) {
		            Predicate<ItemStack> predicate = ((RangedWeaponItem)stack.getItem()).getHeldProjectiles();
		            ItemStack itemStack = RangedWeaponItem.getHeldProjectile(shooter, predicate);
		            return itemStack.isEmpty() ? new ItemStack(Items.ARROW) : itemStack;
		        }
		        return ItemStack.EMPTY;
		    }
		    
		    public <T extends PathAwareEntity & ITricksyMob<?>> void stop(T tricksy, LeafNode parent)
		    {
		    	tricksy.clearActiveItem();
		    }
		});
	}
}
