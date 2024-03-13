package com.lying.tricksy.particle;

import com.lying.tricksy.init.TFParticles;

import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class EnergyEmitterParticle extends NoRenderParticle
{
	private final float red, green, blue;
	
	public EnergyEmitterParticle(ClientWorld world, double x, double y, double z, double r, double g, double b)
	{
		super(world, x, y, z, 0, 0, 0);
		red = (float)r;
		green = (float)g;
		blue = (float)b;
		this.maxAge = 8;
	}
	
	public void tick()
	{
        double d = this.x + (this.random.nextDouble() - 0.5D);
        double e = this.y + (this.random.nextDouble() - 0.5D);
        double f = this.z + (this.random.nextDouble() - 0.5D);
        this.world.addParticle(TFParticles.ENERGY, d, e, f, red, green, blue);
		if(this.age++ == this.maxAge)
			this.markDead();
	}
	
	public static class Factory implements ParticleFactory<DefaultParticleType>
	{
		public Factory(SpriteProvider spriteProvider) { }
		
		public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double x, double y, double z, double velocityX, double velocityY, double velocityZ)
		{
			return new EnergyEmitterParticle(clientWorld, x, y, z, (float)velocityX, (float)velocityY, (float)velocityZ);
		}
	}
}
