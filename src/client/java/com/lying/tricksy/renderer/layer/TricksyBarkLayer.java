package com.lying.tricksy.renderer.layer;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.lying.tricksy.entity.ITricksyMob;
import com.lying.tricksy.entity.ITricksyMob.Bark;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class TricksyBarkLayer<T extends PathAwareEntity & ITricksyMob<?>, M extends EntityModel<T>> extends FeatureRenderer<T, M>
{
	private static final float scale = 0.5F;
	private final EntityRenderDispatcher dispatcher;
	
	public TricksyBarkLayer(FeatureRendererContext<T, M> context)
	{
		super(context);
		this.dispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
	}
	
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T tricksy, float limbAngle, float limbDistance, float age, float headYaw, float headPitch, float tickDelta)
	{
		Bark bark = tricksy.currentBark();
		if(bark == Bark.NONE)
			return;
		
		Identifier barkTex = bark.textureLocation();
		matrices.push();
			matrices.scale(-1F, -1F, 1F);
			float bodyYaw = MathHelper.lerpAngleDegrees((float)tickDelta, (float)tricksy.prevBodyYaw, (float)tricksy.bodyYaw);
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(bodyYaw));
			matrices.push();
				matrices.multiply(dispatcher.getRotation());
				matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
				matrices.translate(0D, tricksy.getHeight(), 0D);
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder consumer = tessellator.getBuffer();
				consumer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
				RenderSystem.setShader(GameRenderer::getPositionTexProgram);
				RenderSystem.setShaderTexture(0, barkTex);
				RenderSystem.enableDepthTest();
				Matrix4f matrix = matrices.peek().getPositionMatrix();
				
				Vector3f[] vecs = new Vector3f[] {new Vector3f(-1.0f, -1.0f, 0.0f), new Vector3f(-1.0f, 1.0f, 0.0f), new Vector3f(1.0f, 1.0f, 0.0f), new Vector3f(1.0f, -1.0f, 0.0f)};
				for(int i=0; i<vecs.length; i++)
					vecs[i] = vecs[i].mul(scale);
				
				consumer.vertex(matrix, vecs[0].x, vecs[0].y, 0F).texture(1F, 1F).next();
				consumer.vertex(matrix, vecs[1].x, vecs[1].y, 0F).texture(1F, 0F).next();
				consumer.vertex(matrix, vecs[2].x, vecs[2].y, 0F).texture(0F, 0F).next();
				consumer.vertex(matrix, vecs[3].x, vecs[3].y, 0F).texture(0F, 1F).next();
				tessellator.draw();
			matrices.pop();
		matrices.pop();
	}
}
