package com.lying.tricksy.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class LevelUpParticle extends SpriteBillboardParticle
{
	private final SpriteProvider spriteProvider;
	
	public LevelUpParticle(ClientWorld clientWorld, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider)
	{
		super(clientWorld, x, y, z, velocityX, velocityY, velocityZ);
		this.velocityX = velocityX;
		this.velocityY = velocityY;
		this.velocityZ = velocityZ;
		this.spriteProvider = spriteProvider;
		this.maxAge = 60 + this.random.nextInt(12);
		this.age = this.random.nextInt(this.maxAge);
		this.setSpriteForAge(this.spriteProvider);
	}
	
	public ParticleTextureSheet getType()
	{
		return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
	}
	
	public int getBrightness(float tint) { return 240; }
	
	public void tick()
	{
		super.tick();
		this.setSpriteForAge(this.spriteProvider);
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
			return new LevelUpParticle(clientWorld, x, y, z, velocityX, velocityY, velocityZ, this.spriteProvider);
		}
	}
}
