package com.lying.tricksy.renderer.entity;

import com.lying.tricksy.api.entity.ITricksyMob.Bark;
import com.lying.tricksy.entity.EntityTricksyWolf;
import com.lying.tricksy.init.TFModelParts;
import com.lying.tricksy.model.entity.ModelTricksyWolfBase;
import com.lying.tricksy.model.entity.ModelTricksyWolfMain;
import com.lying.tricksy.renderer.layer.AbstractOffsetHeldItemLayer;
import com.lying.tricksy.renderer.layer.TricksyBarkLayer;
import com.lying.tricksy.renderer.layer.TricksyWolfBookLayer;
import com.lying.tricksy.renderer.layer.TricksyWolfClothingLayer;

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
public class EntityTricksyWolfRenderer extends MobEntityRenderer<EntityTricksyWolf, ModelTricksyWolfBase<EntityTricksyWolf>>
{
	public static final Identifier TEXTURE = new Identifier("textures/entity/wolf/wolf.png");

//	private final ModelTricksyWolfBase<EntityTricksyWolf> standing;
//	private final ModelTricksyWolfBase<EntityTricksyWolf> sleeping;
	
	public EntityTricksyWolfRenderer(Context ctx)
	{
		super(ctx, new ModelTricksyWolfMain<EntityTricksyWolf>(ctx.getModelLoader().getModelPart(TFModelParts.TRICKSY_WOLF)), 0.5F);
		this.addFeature(new TricksyWolfClothingLayer(this));
		this.addFeature(new TricksyWolfBookLayer(this));
		this.addFeature(new AbstractOffsetHeldItemLayer<EntityTricksyWolf, ModelTricksyWolfBase<EntityTricksyWolf>>(this, ctx.getHeldItemRenderer())
		{
			public void translateToHand(MatrixStack matrices, boolean isLeft)
			{
				matrices.translate(0f, 0.045f, -0.575f);
			}
		});
		
//		this.standing = this.model;
//		this.sleeping = new ModelTricksyGoatSleeping<EntityTricksyWolf>(ctx.getModelLoader().getModelPart(TFModelParts.TRICKSY_GOAT_SLEEPING));
	}
	
	@Override
	public void render(EntityTricksyWolf mobEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i)
	{
//		switch(mobEntity.getTreePose())
//		{
//			case SITTING:
//				this.model = this.sleeping;
//				break;
//			case STANDING:
//			default:
//				this.model = this.standing;
//				break;
//		}
		if(!mobEntity.isTreeSleeping())
			setModelPose(mobEntity);
		
		super.render(mobEntity, f, g, matrixStack, vertexConsumerProvider, i);
		
		if(mobEntity.currentBark() != Bark.NONE && dispatcher.getSquaredDistanceToCamera(mobEntity) <= (32 * 32))
			TricksyBarkLayer.renderBark(mobEntity, mobEntity.currentBark(), matrixStack, vertexConsumerProvider, dispatcher, i);
	}
	
	private void setModelPose(EntityTricksyWolf mobEntity)
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
	
	private static BipedEntityModel.ArmPose getArmPose(EntityTricksyWolf mobEntity, Hand hand)
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
	
	public Identifier getTexture(EntityTricksyWolf entity)
	{
		return TEXTURE;
	}
}
