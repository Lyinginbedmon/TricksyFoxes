package com.lying.tricksy.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class EnergyParticle extends SpriteBillboardParticle
{
	public EnergyParticle(ClientWorld world, double x, double y, double z, float r, float g, float b, SpriteProvider spriteProvider)
	{
		super(world, x, y, z, 0, 0, 0);
		this.red = Math.abs(r) / 255F;
		this.green = Math.abs(g) / 255F;
		this.blue = Math.abs(b) / 255F;
		this.alpha = 0.5F;
		
		this.scale = 0.25F;
		this.ascending = true;
		this.velocityX *= 0.01D;
		this.velocityY = Math.max(Math.abs(this.velocityY), 0.01D);
		this.velocityZ *= 0.01D;
		this.collidesWithWorld = false;
		this.maxAge = this.random.nextBetween(30, 40);
		this.setSpriteForAge(spriteProvider);
	}
	
	public ParticleTextureSheet getType()
	{
		return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
	}
	
	public void move(double dx, double dy, double dz)
	{
		setBoundingBox(getBoundingBox().offset(dx, dy, dz));
		repositionFromBoundingBox();
	}
	
	public float getSize(float tickDelta)
	{
		return this.scale * (1F - ((float)(this.age + tickDelta) / (float)this.maxAge));
	}
	
	public int getBrightness(float tint) { return 255; }
	
	public void tick()
	{
		super.tick();
		setAlpha(0.5F * (1F - ((float)this.age / (float)this.maxAge)));
	}
	
	public static class Factory implements ParticleFactory<DefaultParticleType>
	{
		private final SpriteProvider spriteProvider;
		
		public Factory(SpriteProvider spriteProvider)
		{
			this.spriteProvider = spriteProvider;
		}
		
		public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double x, double y, double z, double velocityX, double velocityY, double velocityZ)
		{
			return new EnergyParticle(clientWorld, x, y, z, (float)velocityX, (float)velocityY, (float)velocityZ, this.spriteProvider);
		}
	}
}
