package com.lying.tricksy.renderer.entity;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public abstract class ModelledEntityRenderer<T extends Entity, M extends EntityModel<T>> extends EntityRenderer<T> implements FeatureRendererContext<T, M>
{
	private static final MinecraftClient mc = MinecraftClient.getInstance();
	private M model;
	protected final List<FeatureRenderer<T, M>> features = Lists.newArrayList();
	
	public ModelledEntityRenderer(Context ctx, M modelIn)
	{
		super(ctx);
		this.model = modelIn;
	}
	
	protected final boolean addFeature(FeatureRenderer<T, M> feature) { return this.features.add(feature); }
	
	public M getModel() { return this.model; }
	
	public void render(T entity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i)
	{
		matrixStack.push();
			float k = MathHelper.lerpAngleDegrees((float)g, (float)entity.prevYaw, (float)entity.getYaw());
			float l = getAnimationProgress(entity, g);
			float m = MathHelper.lerp((float)g, (float)entity.prevPitch, (float)entity.getPitch());
			float n = 0F;
			float o = 0F;
			this.setupTransforms(entity, matrixStack, l, k, g);
			matrixStack.scale(-1F, -1F, 1F);
			matrixStack.translate(0F, -1.501F, 0F);
			this.model.animateModel(entity, o, n, g);
			this.model.setAngles(entity, o, n, l, k, m);
			boolean visible = isVisible(entity);
			boolean invisibleTo = !visible && !entity.isInvisibleTo(mc.player);
			boolean outlined = mc.hasOutline(entity);
			RenderLayer renderLayer = this.getRenderLayer(entity, visible, invisibleTo, outlined);
			if(renderLayer != null)
			{
				VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);
				int p = ModelledEntityRenderer.getOverlay(entity, this.getAnimationCounter(entity, g));
				this.model.render(matrixStack, vertexConsumer, i, p, 1F, 1F, 1F, invisibleTo ? 0.15F : 1F);
			}
			if(!entity.isSpectator())
				this.features.forEach(feature -> feature.render(matrixStack, vertexConsumerProvider, i, entity, o, n, g, l, k, m));
		matrixStack.pop();
		super.render(entity, f, g, matrixStack, vertexConsumerProvider, i);
	}
	
	/**
	 * Gets the render layer appropriate for rendering the passed entity. Returns null if the entity should not be rendered.
	 */
	@Nullable
	protected RenderLayer getRenderLayer(T entity, boolean showBody, boolean translucent, boolean showOutline)
	{
		Identifier identifier = this.getTexture(entity);
		if (translucent)
			return RenderLayer.getItemEntityTranslucentCull(identifier);
		if (showBody)
			return ((Model)this.model).getLayer(identifier);
		if (showOutline)
			return RenderLayer.getOutline(identifier);
		return null;
	}
	
	public static int getOverlay(Entity entity, float whiteOverlayProgress)
	{
		return OverlayTexture.packUv(OverlayTexture.getU(whiteOverlayProgress), OverlayTexture.getV(false));
	}
	
	protected boolean isVisible(T entity) { return !entity.isInvisible(); }
	
	protected float getAnimationCounter(T entity, float tickDelta) { return 0F; }
	
	protected float getAnimationProgress(T entity, float tickDelta) { return (float)entity.age + tickDelta; }
	
	protected void setupTransforms(T entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta)
	{
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - bodyYaw));
	}
}
