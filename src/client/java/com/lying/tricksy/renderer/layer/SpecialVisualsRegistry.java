package com.lying.tricksy.renderer.layer;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Matrix4f;

import com.lying.tricksy.init.TFParticles;
import com.lying.tricksy.init.TFSpecialVisual;
import com.lying.tricksy.reference.Reference;
import com.lying.tricksy.renderer.layer.SpecialVisualsLayer.SpecialVisualRender;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class SpecialVisualsRegistry
{
	public static final Event<AssignVisual> ASSIGN_VISUAL = EventFactory.createArrayBacked(AssignVisual.class, callbacks -> (registry) -> 
	{
		for(AssignVisual callback : callbacks)
			callback.registerVisuals(registry);
	});
	
	@FunctionalInterface
	public interface AssignVisual
	{
		void registerVisuals(SpecialVisualsRegistry registry);
	}
	
	public static SpecialVisualsRegistry INSTANCE = new SpecialVisualsRegistry();
	
	private final Map<TFSpecialVisual, SpecialVisualRender> visualRenderers = new HashMap<>();
	
	public static SpecialVisualsRegistry instance() { return INSTANCE; }
	
	public void register(TFSpecialVisual visual, SpecialVisualRender renderer)
	{
		visualRenderers.put(visual, renderer);
	}
	
	public boolean hasRender(TFSpecialVisual visual) { return visualRenderers.containsKey(visual); }
	
	@Nullable
	public SpecialVisualRender getRender(TFSpecialVisual visual) { return visualRenderers.getOrDefault(visual, null); }
	
	public void init()
	{
		register(TFSpecialVisual.GOAT_JUMP, (matrixStack, vertexConsumerProvider, light, entity, tickDelta, ageInTicks, ticksActive, progress) -> 
		{
			if(ticksActive > 0)
				return;
			
			Random rand = ((LivingEntity)entity).getRandom();
			for(int i=0; i<5; i++)
			{
				double x = rand.nextDouble() - 0.5D;
				double z = rand.nextDouble() - 0.5D;
				Vec3d vel = (new Vec3d(x, 0, z)).normalize();
				entity.getWorld().addParticle(ParticleTypes.CLOUD, entity.getX(), entity.getY(), entity.getZ(), vel.x, 0, vel.z);
			}
		});
		register(TFSpecialVisual.WOLF_BLESS, (matrixStack, vertexConsumerProvider, light, entity, tickDelta, ageInTicks, ticksActive, progress) -> 
		{
			if(ticksActive%10 == 0)
				entity.getWorld().addParticle(TFParticles.ENERGY_EMITTER, entity.getX(), entity.getY() + 0.5D, entity.getZ(), 252, 248, 205);
		});
		register(TFSpecialVisual.ONRYOJI_BALANCE, new SpecialVisualRender()
		{
			private static final Identifier TEXTURE_CIRCLE = new Identifier(Reference.ModInfo.MOD_ID, "textures/entity/magic_circle.png");
			
			public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, Entity entity, float tickDelta, float ageInTicks, int ticksActive, float progress)
			{
				VertexConsumer buffer = vertexConsumerProvider.getBuffer(RenderLayer.getText(TEXTURE_CIRCLE));
				Matrix4f matrix = matrixStack.peek().getPositionMatrix();
				
				float time = (float)ticksActive + tickDelta;
				float radius = 1F + (0.3F * (1F - Math.clamp(0F, 1F, time / 10F)));
				int alpha = (int)(Math.clamp(0F, 1F, (float)ticksActive / 10F) * 255);
				float rotation = time * 10F;
				
				Vec2f[] vertices = new Vec2f[]{
						new Vec2f(-radius, -radius),
						new Vec2f(+radius, -radius),
						new Vec2f(+radius, +radius),
						new Vec2f(-radius, +radius)};
				
				for(int i=0; i<vertices.length; i++)
					vertices[i] = rotateVec(vertices[i], rotation);
				
				matrixStack.push();
					buffer.vertex(matrix, vertices[0].x, 0F, vertices[0].y).color(255, 255, 255, alpha).texture(0F, 0F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).next();
					buffer.vertex(matrix, vertices[1].x, 0F, vertices[1].y).color(255, 255, 255, alpha).texture(1F, 0F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).next();
					buffer.vertex(matrix, vertices[2].x, 0F, vertices[2].y).color(255, 255, 255, alpha).texture(1F, 1F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).next();
					buffer.vertex(matrix, vertices[3].x, 0F, vertices[3].y).color(255, 255, 255, alpha).texture(0F, 1F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).next();
					
					buffer.vertex(matrix, vertices[3].x, 0F, vertices[3].y).color(255, 255, 255, alpha).texture(0F, 1F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).next();
					buffer.vertex(matrix, vertices[2].x, 0F, vertices[2].y).color(255, 255, 255, alpha).texture(1F, 1F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).next();
					buffer.vertex(matrix, vertices[1].x, 0F, vertices[1].y).color(255, 255, 255, alpha).texture(1F, 0F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).next();
					buffer.vertex(matrix, vertices[0].x, 0F, vertices[0].y).color(255, 255,255, alpha).texture(0F, 0F).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).next();
				matrixStack.pop();
			}
			
			private static Vec2f rotateVec(Vec2f vec, float degrees)
			{
				float rads = Math.toRadians(degrees);
				float cos = (float)Math.cos(rads);
				float sin = (float)Math.sin(rads);
				
				float x = (float)(vec.x * cos) - (float)(vec.y * sin);
				float y = (float)(vec.x * sin) + (float)(vec.y * cos);
				return new Vec2f(x, y);
			}
		});
		ASSIGN_VISUAL.invoker().registerVisuals(this);
	}
}
