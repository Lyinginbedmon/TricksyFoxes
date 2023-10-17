package com.lying.tricksy.entity.ai.node.subtype;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import com.google.common.collect.Lists;
import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ai.node.LeafNode;
import com.lying.tricksy.entity.ai.node.TreeNode.Result;
import com.lying.tricksy.entity.ai.node.handler.CombatHandler;
import com.lying.tricksy.entity.ai.node.handler.INodeInput;
import com.lying.tricksy.entity.ai.node.handler.NodeTickHandler;
import com.lying.tricksy.entity.ai.node.handler.RangedCombatHandler;
import com.lying.tricksy.entity.ai.whiteboard.CommonVariables;
import com.lying.tricksy.entity.ai.whiteboard.ConstantsWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.GlobalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.IWhiteboardObject;
import com.lying.tricksy.entity.ai.whiteboard.LocalWhiteboard;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObj;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjBlock;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardObjEntity;
import com.lying.tricksy.entity.ai.whiteboard.WhiteboardRef;
import com.lying.tricksy.init.TFObjType;
import com.lying.tricksy.mixin.CrossbowItemInvoker;
import com.lying.tricksy.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LeafCombat implements ISubtypeGroup<LeafNode>
{
	public static final Identifier VARIANT_SET_ATTACK = ISubtypeGroup.variant("set_attack");
	public static final Identifier VARIANT_ATTACK_MELEE = ISubtypeGroup.variant("melee_attack");
	public static final Identifier VARIANT_ATTACK_BOW = ISubtypeGroup.variant("bow_attack");
	public static final Identifier VARIANT_ATTACK_TRIDENT = ISubtypeGroup.variant("trident_attack");
	public static final Identifier VARIANT_ATTACK_CROSSBOW = ISubtypeGroup.variant("crossbow_attack");
	public static final Identifier VARIANT_ATTACK_POTION = ISubtypeGroup.variant("potion_attack");
	public static final Identifier VARIANT_SHIELD = ISubtypeGroup.variant("shield_against");
	
	public Text displayName() { return Text.translatable("subtype."+Reference.ModInfo.MOD_ID+".leaf_combat"); }
	
	public Collection<NodeSubType<LeafNode>> getSubtypes()
	{
		List<NodeSubType<LeafNode>> set = Lists.newArrayList();
		set.add(new NodeSubType<LeafNode>(VARIANT_SET_ATTACK, setAttackTarget()));
		set.add(new NodeSubType<LeafNode>(VARIANT_ATTACK_MELEE, meleeAttack()));
		set.add(new NodeSubType<LeafNode>(VARIANT_ATTACK_BOW, bowAttack()));
		set.add(new NodeSubType<LeafNode>(VARIANT_ATTACK_TRIDENT, tridentAttack()));
		set.add(new NodeSubType<LeafNode>(VARIANT_ATTACK_CROSSBOW, crossbowAttack()));
		set.add(new NodeSubType<LeafNode>(VARIANT_ATTACK_POTION, potionAttack()));
		set.add(new NodeSubType<LeafNode>(VARIANT_SHIELD, shieldAgainst()));
		return set;
	}
	
	private static NodeTickHandler<LeafNode> setAttackTarget()
	{
		return new NodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.TARGET_ENT, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.ENT, false), new WhiteboardObjEntity()));
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
		};
	}
	
	private static NodeTickHandler<LeafNode> meleeAttack()
	{
		return new CombatHandler()
		{
			protected <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result attack(T tricksy, LivingEntity target, LocalWhiteboard<T> local, LeafNode parent)
			{
				if(tricksy.isInAttackRange(target) && !target.isInvulnerable())
				{
					NodeTickHandler.swingHand(tricksy, Hand.MAIN_HAND);
					tricksy.logStatus(Text.literal("Have at you!"));
					return tricksy.tryAttack(target) ? Result.SUCCESS : Result.FAILURE;
				}
				return Result.FAILURE;
			}
		};
	}
	
	private static NodeTickHandler<LeafNode> potionAttack()
	{
		return new RangedCombatHandler()
		{
			public boolean isRangeWeapon(ItemStack bowStack) { return bowStack.isOf(Items.SPLASH_POTION) || bowStack.isOf(Items.LINGERING_POTION); }
			
			public int getDrawTime() { return 0; }
			
			protected void attack(LivingEntity target, ItemStack bowStack, float pullProgress, PathAwareEntity shooter)
			{
				Vec3d targetVel = target.getVelocity();
		        double offsetX = target.getX() + targetVel.x - shooter.getX();
		        double offsetY = target.getEyeY() - (double)1.1f - shooter.getY();
		        double offsetZ = target.getZ() + targetVel.z - shooter.getZ();
		        double distance = Math.sqrt(offsetX * offsetX + offsetZ * offsetZ);
		        Potion potion = PotionUtil.getPotion(bowStack);
		        PotionEntity potionEntity = new PotionEntity(shooter.getWorld(), shooter);
		        potionEntity.setItem(PotionUtil.setPotion(new ItemStack(Items.SPLASH_POTION), potion));
		        potionEntity.setPitch(potionEntity.getPitch() - -20.0f);
		        potionEntity.setVelocity(offsetX, offsetY + distance * 0.2, offsetZ, 0.75f, 8.0f);
		        if(!shooter.isSilent())
		            shooter.getWorld().playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.ENTITY_WITCH_THROW, shooter.getSoundCategory(), 1.0f, 0.8f + shooter.getRandom().nextFloat() * 0.4f);
		        shooter.getWorld().spawnEntity(potionEntity);
			}
			
			protected void attack(LivingEntity target, float pullProgress, PathAwareEntity shooter) { }
		};
	}
	
	private static NodeTickHandler<LeafNode> tridentAttack()
	{
		return new RangedCombatHandler() 
		{
			public boolean isRangeWeapon(ItemStack bowStack) { return bowStack.isOf(Items.TRIDENT); }
			
			public int getDrawTime() { return Reference.Values.TICKS_PER_SECOND; }
			
			protected void attack(LivingEntity target, float pullProgress, PathAwareEntity shooter)
			{
		        TridentEntity tridentEntity = new TridentEntity(shooter.getWorld(), (LivingEntity)shooter, new ItemStack(Items.TRIDENT));
		        double offsetX = target.getX() - shooter.getX();
		        double offsetY = target.getBodyY(0.3333333333333333) - tridentEntity.getY();
		        double offsetZ = target.getZ() - shooter.getZ();
		        double distance = Math.sqrt(offsetX * offsetX + offsetZ * offsetZ);
		        tridentEntity.setVelocity(offsetX, offsetY + distance * (double)0.2f, offsetZ, 1.6f, 14 - shooter.getWorld().getDifficulty().getId() * 4);
		        shooter.playSound(SoundEvents.ENTITY_DROWNED_SHOOT, 1.0f, 1.0f / (shooter.getRandom().nextFloat() * 0.4f + 0.8f));
		        shooter.getWorld().spawnEntity(tridentEntity);
			}
		};
	}
	
	private static NodeTickHandler<LeafNode> bowAttack()
	{
		return new RangedCombatHandler()
		{
			private static final WhiteboardRef DRAW = new WhiteboardRef("draw_time", TFObjType.INT).displayName(CommonVariables.translate("draw_time"));
			/** How long the bow should be drawn */
			private int drawTicks = 0;
			
			protected void addVariables(Map<WhiteboardRef, INodeInput> set)
			{
				set.put(DRAW, INodeInput.makeInput(NodeTickHandler.ofType(TFObjType.INT, true), new WhiteboardObj.Int(1), ConstantsWhiteboard.NUM_1.displayName()));
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
			    double e = target.getBodyY(1D/3D) - arrow.getY();
			    double f = target.getZ() - shooter.getZ();
			    double g = Math.sqrt(d * d + f * f);
			    
			    World world = shooter.getWorld();
			    arrow.setVelocity(d, e + g * (double)0.2f, f, 1.6f, 14 - world.getDifficulty().getId() * 4);
			    if(pullProgress >= 1F)
			    	arrow.setCritical(true);
			    shooter.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0f, 1.0f / (shooter.getRandom().nextFloat() * 0.4f + 0.8f));
			    world.spawnEntity(arrow);
			}
		};
	}
	
	
	private static NodeTickHandler<LeafNode> crossbowAttack()
	{
		return new RangedCombatHandler()
		{
			protected <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result attack(T tricksy, LivingEntity target, LocalWhiteboard<T> local, LeafNode parent)
			{
				ItemStack bowStack = tricksy.getMainHandStack();
				if(bowStack.isEmpty() || !bowStack.isOf(Items.CROSSBOW))
					return Result.FAILURE;
				
				switch(getStage(bowStack, tricksy.getActiveItem().equals(bowStack)))
				{
					case UNCHARGED:
						tricksy.logStatus(Text.literal("Draw!"));
						tricksy.setCurrentHand(Hand.MAIN_HAND);
						break;
					case CHARGING:
						if(tricksy.getItemUseTime() >= CrossbowItem.getPullTime(tricksy.getActiveItem()))
							tricksy.stopUsingItem();
						break;
					case CHARGED:
						return super.attack(tricksy, target, local, parent);
				}
				
				return Result.RUNNING;
			}
			
			public boolean isRangeWeapon(ItemStack bowStack) { return bowStack.isOf(Items.CROSSBOW); }
			
			public int getDrawTime() { return 0; }
			
			protected void attack(LivingEntity target, float pullProgress, PathAwareEntity shooter) 
			{
				shootAll(target, shooter.getWorld(), shooter, Hand.MAIN_HAND, shooter.getMainHandStack(), 1.6F, 14 - shooter.getWorld().getDifficulty().getId() * 4);
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> void onEnd(T tricksy, LeafNode parent)
			{
				super.onEnd(tricksy, parent);
				ItemStack bowStack = tricksy.getMainHandStack();
				CrossbowItemInvoker.tricksy$postShoot(tricksy.getEntityWorld(), tricksy, bowStack);
				tricksy.setStackInHand(Hand.MAIN_HAND, bowStack);
			}
			
			public void shootAll(LivingEntity target, World world, LivingEntity shooter, Hand hand, ItemStack stack, float speed, float divergence)
			{
				List<ItemStack> projectiles = CrossbowItemInvoker.tricksy$getProjectiles(stack);
				float[] pitches = CrossbowItemInvoker.tricksy$getSoundPitches(shooter.getRandom());
				for(int i=0; i < projectiles.size(); ++i)
				{
					ItemStack ammo = projectiles.get(i);
					shoot(target, world, shooter, hand, stack, ammo, pitches[i%3], false, speed, divergence, i%3 == 0 ? 0F : i%3 == 1 ? -10F : 10F);
				}
			}
			
			public void shoot(LivingEntity target, World world, LivingEntity shooter, Hand hand, ItemStack crossbow, ItemStack projectile, float soundPitch, boolean creative, float speed, float divergence, float simulated)
			{
				ProjectileEntity projectileEntity;
				if(world.isClient())
					return;
				boolean isRocket = projectile.isOf(Items.FIREWORK_ROCKET);
				if(isRocket)
					projectileEntity = new FireworkRocketEntity(world, projectile, shooter, shooter.getX(), shooter.getEyeY() - (double)0.15f, shooter.getZ(), true);
				else
					projectileEntity = CrossbowItemInvoker.tricksy$createArrow(world, shooter, crossbow, projectile);
				shootAt(target, shooter, projectileEntity, simulated, 1.6F);
				crossbow.damage(isRocket ? 3 : 1, shooter, e -> e.sendToolBreakStatus(hand));
				world.spawnEntity(projectileEntity);
				world.playSound(null,  shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.ITEM_CROSSBOW_SHOOT, SoundCategory.PLAYERS, 1F, soundPitch);
			}
			
			public void shootAt(LivingEntity target, LivingEntity entity, ProjectileEntity projectile, float multishotSpray, float speed)
			{
		        double offsetX = target.getX() - entity.getX();
		        double offsetZ = target.getZ() - entity.getZ();
		        double flatDist = Math.sqrt(offsetX * offsetX + offsetZ * offsetZ);
		        double yAim = target.getBodyY(0.3333333333333333) - projectile.getY() + flatDist * (double)0.2f;
		        Vector3f velocity = getLaunchVelocity(entity, new Vec3d(offsetX, yAim, offsetZ), multishotSpray);
		        projectile.setVelocity(velocity.x(), velocity.y(), velocity.z(), speed, 14 - entity.getWorld().getDifficulty().getId() * 4);
		        entity.playSound(SoundEvents.ITEM_CROSSBOW_SHOOT, 1.0f, 1.0f / (entity.getRandom().nextFloat() * 0.4f + 0.8f));
		    }
			
			public Vector3f getLaunchVelocity(LivingEntity entity, Vec3d positionDelta, float multishotSpray)
			{
		        Vector3f vector3f = positionDelta.toVector3f().normalize();
		        Vector3f vector3f2 = new Vector3f(vector3f).cross(new Vector3f(0.0f, 1.0f, 0.0f));
		        if ((double)vector3f2.lengthSquared() <= 1.0E-7) {
		            Vec3d vec3d = entity.getOppositeRotationVector(1.0f);
		            vector3f2 = new Vector3f(vector3f).cross(vec3d.toVector3f());
		        }
		        Vector3f vector3f3 = new Vector3f(vector3f).rotateAxis(1.5707964f, vector3f2.x, vector3f2.y, vector3f2.z);
		        return new Vector3f(vector3f).rotateAxis(multishotSpray * ((float)Math.PI / 180), vector3f3.x, vector3f3.y, vector3f3.z);
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
		};
	}
	
	// FIXME Ensure that tricksy shield actually works properly
	private static NodeTickHandler<LeafNode> shieldAgainst()
	{
		return new NodeTickHandler<LeafNode>()
		{
			public Map<WhiteboardRef, INodeInput> variableSet()
			{
				return Map.of(CommonVariables.TARGET_ENT, INodeInput.makeInput(
						(var) -> !var.isSameRef(LocalWhiteboard.SELF) && (var.type() == TFObjType.BLOCK || (var.type() == TFObjType.ENT && !var.isFilter())), 
						new WhiteboardObjEntity(), 
						LocalWhiteboard.ATTACK_TARGET.displayName()));
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> @NotNull Result doTick(T tricksy, LocalWhiteboard<T> local, GlobalWhiteboard global, LeafNode parent)
			{
				if(!tricksy.getMainHandStack().isOf(Items.SHIELD))
					return Result.FAILURE;
				
				tricksy.setCurrentHand(Hand.MAIN_HAND);
				IWhiteboardObject<?> target = getOrDefault(CommonVariables.TARGET_ENT, parent, local, global);
				if(target.size() > 0)
					if(target.type() == TFObjType.BLOCK)
					{
						Direction face = ((WhiteboardObjBlock)target.as(TFObjType.BLOCK)).direction();
						
						float yaw = tricksy.bodyYaw;
						float pitch = 0F;
						if(face.getAxis() == Axis.Y)
							pitch = face == Direction.UP ? 90F : -90F;
						else
							yaw = face.asRotation();
						
						Vec3d pos = tricksy.getPos();
						tricksy.refreshPositionAndAngles(pos.x, pos.y, pos.z, yaw, pitch);
						tricksy.setHeadYaw(yaw);
					}
					else if(target.type() == TFObjType.ENT)
					{
						Entity ent = target.as(TFObjType.ENT).size() == 0 ? tricksy.getAttacking() : target.as(TFObjType.ENT).get();
						if(ent != null)
							tricksy.getLookControl().lookAt(ent);
					}
				
				return Result.RUNNING;
			}
			
			public <T extends PathAwareEntity & ITricksyMob<?>> void onEnd(T tricksy, LeafNode parent)
			{
				tricksy.stopUsingItem();
			}
		};
	}
}
