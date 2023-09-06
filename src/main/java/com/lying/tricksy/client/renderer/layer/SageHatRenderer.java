package com.lying.tricksy.client.renderer.layer;

import com.lying.tricksy.client.TFModelParts;
import com.lying.tricksy.client.model.armor.ModelSageHat;
import com.lying.tricksy.reference.Reference;

import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class SageHatRenderer implements ArmorRenderer
{
	private static final Identifier TEXTURE_0 = new Identifier(Reference.ModInfo.MOD_ID, "textures/armor/sage_hat_0.png");
	private static final Identifier TEXTURE_1 = new Identifier(Reference.ModInfo.MOD_ID, "textures/armor/sage_hat_1.png");
	
	private ModelSageHat<LivingEntity> model = null;
	
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack, LivingEntity entity, EquipmentSlot slot, int light, BipedEntityModel<LivingEntity> contextModel)
	{
		if(model == null)
		{
			model = new ModelSageHat<LivingEntity>(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(TFModelParts.SAGE_HAT));
			
			model.setVisible(false);
			model.head.visible = true;
		}
		
		contextModel.copyBipedStateTo(model);
		ArmorRenderer.renderPart(matrices, vertexConsumers, light, stack, model, TEXTURE_0);
		
		int col = ((DyeableItem)stack.getItem()).getColor(stack);
        float r = (float)(col >> 16 & 0xFF) / 255.0f;
        float g = (float)(col >> 8 & 0xFF) / 255.0f;
        float b = (float)(col & 0xFF) / 255.0f;
        VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumers, RenderLayer.getArmorCutoutNoCull(TEXTURE_1), false, stack.hasGlint());
        model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, r, g, b, 1);
	}
}
