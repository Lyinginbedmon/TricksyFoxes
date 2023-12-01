package com.lying.tricksy.renderer.entity;

import com.lying.tricksy.entity.EntityTricksyGoat;
import com.lying.tricksy.entity.ITricksyMob.Bark;
import com.lying.tricksy.init.TFModelParts;
import com.lying.tricksy.model.entity.ModelTricksyGoatBase;
import com.lying.tricksy.model.entity.ModelTricksyGoatMain;
import com.lying.tricksy.renderer.layer.TricksyBarkLayer;
import com.lying.tricksy.renderer.layer.TricksyGoatClothingLayer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class EntityTricksyGoatRenderer extends MobEntityRenderer<EntityTricksyGoat, ModelTricksyGoatBase<EntityTricksyGoat>>
{
	public static final Identifier TEXTURE_RED = new Identifier("textures/entity/fox/fox.png");
	public static final Identifier TEXTURE_SNOW = new Identifier("textures/entity/fox/snow_fox.png");
	
	public static final Identifier TEXTURE_RED_SLEEPING = new Identifier("textures/entity/fox/fox_sleep.png");
	public static final Identifier TEXTURE_SNOW_SLEEPING = new Identifier("textures/entity/fox/snow_fox_sleep.png");
	
	public EntityTricksyGoatRenderer(Context ctx)
	{
		super(ctx, new ModelTricksyGoatMain<EntityTricksyGoat>(ctx.getModelLoader().getModelPart(TFModelParts.TRICKSY_GOAT)), 0.5F);
		this.addFeature(new TricksyGoatClothingLayer(this));
//		this.addFeature(new TricksyFoxHeldItemLayer(this, ctx.getHeldItemRenderer()));
//		this.addFeature(new TricksyBarkLayer<EntityTricksyGoat, ModelTricksyFoxBase<EntityTricksyGoat>>(this));	TODO Improve bark rendering as FeatureRenderer
	}
	
	@Override
	public void render(EntityTricksyGoat mobEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i)
	{
		if(!mobEntity.isSleeping())
			setModelPose(mobEntity);
		
		super.render(mobEntity, f, g, matrixStack, vertexConsumerProvider, i);
		
		if(mobEntity.currentBark() != Bark.NONE && dispatcher.getSquaredDistanceToCamera(mobEntity) <= (32 * 32))
			TricksyBarkLayer.renderBark(mobEntity, mobEntity.currentBark(), matrixStack, vertexConsumerProvider, dispatcher, i);
	}
	
	private void setModelPose(EntityTricksyGoat mobEntity)
	{
		BipedEntityModel.ArmPose mainPose = getArmPose(mobEntity, Hand.MAIN_HAND);
		BipedEntityModel.ArmPose offPose = getArmPose(mobEntity, Hand.OFF_HAND);
		if(mainPose.isTwoHanded())
			offPose = mobEntity.getOffHandStack().isEmpty() ? BipedEntityModel.ArmPose.EMPTY : BipedEntityModel.ArmPose.ITEM;
		switch(mobEntity.getMainArm())
		{
		case LEFT:
			this.model.leftArmPose = mainPose;
			this.model.rightArmPose = offPose;
			break;
		case RIGHT:
			this.model.rightArmPose = mainPose;
			this.model.leftArmPose = offPose;
			break;
		}
	}
	
	private static BipedEntityModel.ArmPose getArmPose(EntityTricksyGoat mobEntity, Hand hand)
	{
		ItemStack heldStack = mobEntity.getStackInHand(hand);
		if(heldStack.isEmpty())
			return BipedEntityModel.ArmPose.EMPTY;
		else if(mobEntity.getActiveHand() == hand && mobEntity.getItemUseTimeLeft() > 0)
			switch(heldStack.getUseAction())
			{
				case BLOCK:
					return BipedEntityModel.ArmPose.BLOCK;
				case BOW:
					return BipedEntityModel.ArmPose.BOW_AND_ARROW;
				case BRUSH:
					return BipedEntityModel.ArmPose.BRUSH;
				case CROSSBOW:
					if(hand == mobEntity.getActiveHand())
						return BipedEntityModel.ArmPose.CROSSBOW_CHARGE;
					return BipedEntityModel.ArmPose.EMPTY;
				case SPEAR:
					return BipedEntityModel.ArmPose.THROW_SPEAR;
				case SPYGLASS:
					return BipedEntityModel.ArmPose.SPYGLASS;
				case TOOT_HORN:
					return BipedEntityModel.ArmPose.TOOT_HORN;
				case DRINK:
				case EAT:
				case NONE:
				default:
					return BipedEntityModel.ArmPose.EMPTY;
			}
		else if(!mobEntity.handSwinging && heldStack.isOf(Items.CROSSBOW) && CrossbowItem.isCharged(heldStack))
			return BipedEntityModel.ArmPose.CROSSBOW_HOLD;
		
		return BipedEntityModel.ArmPose.ITEM;
	}
	
	public Identifier getTexture(EntityTricksyGoat entity)
	{
		return new Identifier("textures/entity/goat/goat.png");
	}
}
