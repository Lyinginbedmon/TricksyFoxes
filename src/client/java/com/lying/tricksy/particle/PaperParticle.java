package com.lying.tricksy.particle;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.lying.tricksy.reference.Reference;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.AbstractSlowingParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class PaperParticle extends AbstractSlowingParticle
{
	private float roll = 0F;
	private final double rollD, yaw;
	
	protected PaperParticle(ClientWorld world, double posX, double posY, double posZ, double velX, double velY, double velZ)
	{
		super(world, posX, posY, posZ, velX, velY, velZ);
		this.collidesWithWorld = false;
		this.gravityStrength = 0F;
		this.maxAge = (int)(Reference.Values.TICKS_PER_SECOND * (0.8F + random.nextFloat() * 0.2F));
		
		updateAlpha();
		
		this.yaw = (random.nextFloat() - 0.5F) * 15F;
		this.roll = (random.nextFloat() - 0.5F) * 45F;
		this.rollD = (random.nextFloat() - 0.5F);
	}
	
	public ParticleTextureSheet getType() { return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT; }
	
	public void updateAlpha()
	{
		float point = MathHelper.clamp((float)(age - (maxAge - 7)), 0F, 7F);
		setAlpha(1F - (point / 7F));
	}
	
	public void tick()
	{
		updateAlpha();
		
		this.roll += this.rollD;
		
		super.tick();
	}
	
	public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta)
	{
		Vec3d camPos = camera.getPos();
		float posX = (float)(MathHelper.lerp((double)tickDelta, this.prevPosX, this.x) - camPos.x);
		float posY = (float)(MathHelper.lerp((double)tickDelta, this.prevPosY, this.y) - camPos.y);
		float posZ = (float)(MathHelper.lerp((double)tickDelta, this.prevPosZ, this.z) - camPos.z);
		
		Quaternionf quaternion = camera.getRotation();
		quaternion.mul(new Quaternionf().setAngleAxis(Math.toRadians(yaw), 0D, 1D, 0D));
		quaternion.mul(new Quaternionf().setAngleAxis(Math.toRadians(roll), 0D, 0D, 1D));
		
		float quadSize = getSize(tickDelta);
		Vector3f[] vertices = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0F), new Vector3f(-1.0F, 1.0F, 0F), new Vector3f(1.0F, 1.0F, 0F), new Vector3f(1.0F, -1.0F, 0F)};
		for(Vector3f vec : vertices)
		{
			vec.rotate(quaternion);
			vec.mul(quadSize);
			vec.add(posX, posY, posZ);
		}
		
		float u0 = this.getMinU();
		float u1 = this.getMaxU();
		float v0 = this.getMinV();
		float v1 = this.getMaxV();
		int light = this.getBrightness(tickDelta);
		
		vertexConsumer.vertex((double)vertices[0].x(), (double)vertices[0].y(), (double)vertices[0].z()).texture(u1, v1).color(1F, 1F, 1F, this.alpha).light(light).next();
		vertexConsumer.vertex((double)vertices[1].x(), (double)vertices[1].y(), (double)vertices[1].z()).texture(u1, v0).color(1F, 1F, 1F, this.alpha).light(light).next();
		vertexConsumer.vertex((double)vertices[2].x(), (double)vertices[2].y(), (double)vertices[2].z()).texture(u0, v0).color(1F, 1F, 1F, this.alpha).light(light).next();
		vertexConsumer.vertex((double)vertices[3].x(), (double)vertices[3].y(), (double)vertices[3].z()).texture(u0, v1).color(1F, 1F, 1F, this.alpha).light(light).next();
		
		vertexConsumer.vertex((double)vertices[3].x(), (double)vertices[3].y(), (double)vertices[3].z()).texture(u0, v0).color(1F, 1F, 1F, this.alpha).light(light).next();
		vertexConsumer.vertex((double)vertices[2].x(), (double)vertices[2].y(), (double)vertices[2].z()).texture(u0, v1).color(1F, 1F, 1F, this.alpha).light(light).next();
		vertexConsumer.vertex((double)vertices[1].x(), (double)vertices[1].y(), (double)vertices[1].z()).texture(u1, v1).color(1F, 1F, 1F, this.alpha).light(light).next();
		vertexConsumer.vertex((double)vertices[0].x(), (double)vertices[0].y(), (double)vertices[0].z()).texture(u1, v0).color(1F, 1F, 1F, this.alpha).light(light).next();
	}
	
	public static class Factory implements ParticleFactory<DefaultParticleType>
	{
		private final SpriteProvider sprites;
		
		public Factory(SpriteProvider spritesIn)
		{
			this.sprites = spritesIn;
		}
		
		public Particle createParticle(DefaultParticleType particleEffect, ClientWorld clientWorld, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
		{
			PaperParticle particle = new PaperParticle(clientWorld, x, y, z, xSpeed, ySpeed, zSpeed);
			particle.setSprite(this.sprites);
			return particle;
		}
	}
}
