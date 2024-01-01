package com.lying.tricksy.renderer.layer;

import com.lying.tricksy.component.TricksyComponent;
import com.lying.tricksy.entity.EntityTricksyFox;
import com.lying.tricksy.init.TFModelParts;
import com.lying.tricksy.model.entity.ModelTricksyFoxBase;
import com.lying.tricksy.model.entity.ModelTricksyFoxCrouching;
import com.lying.tricksy.model.entity.ModelTricksyFoxMain;
import com.lying.tricksy.reference.Reference;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class TricksyFoxMaskLayer extends FeatureRenderer<EntityTricksyFox, ModelTricksyFoxBase<EntityTricksyFox>>
{
	public static final Identifier texture = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/tricksy_fox/mask.png");
	
	private final ModelTricksyFoxBase<EntityTricksyFox> standing;
	private final ModelTricksyFoxBase<EntityTricksyFox> crouching;
	
	protected ModelTricksyFoxBase<EntityTricksyFox> clothingModel;
	
	public TricksyFoxMaskLayer(FeatureRendererContext<EntityTricksyFox, ModelTricksyFoxBase<EntityTricksyFox>> context)
	{
		super(context);
		
		this.standing = new ModelTricksyFoxMain<EntityTricksyFox>(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(TFModelParts.TRICKSY_FOX_MASK));
		this.crouching = new ModelTricksyFoxCrouching<EntityTricksyFox>(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(TFModelParts.TRICKSY_FOX_CROUCHING_MASK));
	}
	
	public boolean shouldRender(EntityTricksyFox living) { return TricksyComponent.isMobMaster(living); }
	
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, EntityTricksyFox living, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, float tickDelta)
	{
		if(living.isInvisible() || !shouldRender(living))
			return;
		
		switch(living.getTreePose())
		{
			case CROUCHING:
				this.clothingModel = this.crouching;
				break;
			case STANDING:
			default:
				this.clothingModel = this.standing;
				break;
		}
		
		getContextModel().copyModelStateTo(this.clothingModel);
		clothingModel.render(matrices, vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(texture)), light, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
	}
}
