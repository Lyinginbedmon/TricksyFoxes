package com.lying.tricksy.particle;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AuraParticle extends SpriteBillboardParticle
{
    private static final Vector3f[] VERTICES = new Vector3f[]{
    		new Vector3f(-1F, -1F, 0F), 
    		new Vector3f(-1F, 1F, 0F), 
    		new Vector3f(1F, 1F, 0F), 
    		new Vector3f(1F, -1F, 0F)};
    
	public AuraParticle(ClientWorld world, double x, double y, double z, float r, float g, float b, SpriteProvider spriteProvider)
	{
		super(world, x, y, z, 0D, 0D, 0D);
		this.velocityMultiplier = 0.66F;
		this.ascending = true;
		this.velocityX *= 0.01D;
		this.velocityY *= 0.01D;
		this.velocityZ *= 0.01D;
		this.red = Math.abs(r) / 255F;
		this.green = Math.abs(g) / 255F;
		this.blue = Math.abs(b) / 255F;
		this.scale = 1F;
		this.maxAge = this.random.nextBetween(8, 16);
		this.collidesWithWorld = false;
		this.setSpriteForAge(spriteProvider);
	}
	
	public ParticleTextureSheet getType()
	{
		return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
	}
	
	public float lifespan(float tickDelta) { return ((float)this.age + tickDelta) / (float)this.maxAge; }
	
	public float getSize(float tickDelta)
	{
		float age = (float)this.age + tickDelta;
		float ageMax = (float)this.maxAge * 16F;
		return this.scale * (float)Math.pow((age / ageMax), 0.05F);
	}
	
	public int getBrightness(float tint) { return 255; }
	
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta)
    {
        Vec3d camPos = camera.getPos();
        float offsetX = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - camPos.getX());
        float offsetY = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - camPos.getY());
        float offsetZ = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - camPos.getZ());
        // Always face the client player but only on the yaw axis
        Quaternionf rotation = (new Quaternionf()).rotateY((float)Math.toRadians(-camera.getYaw()));
        
        float size = this.getSize(tickDelta);
        
        float minU = this.getMinU();
        float maxU = this.getMaxU();
        float minV = this.getMinV();
        float maxV = this.getMaxV();
        int light = this.getBrightness(tickDelta);
        float alpha = this.alpha * (1F - (float)Math.pow(lifespan(0F), 7F)) * 0.7F;
        
        for(int i=0; i<4; ++i)
        {
            Vector3f vec3f = new Vector3f(VERTICES[i].x, VERTICES[i].y, VERTICES[i].z);
            float u = vec3f.x < 0 ? minU : maxU;
            float v = vec3f.y < 0 ? minV : maxV;
            
            vec3f.rotate(rotation);
            vec3f.mul(0.2F, size, 0.2F);
            vec3f.add(offsetX, offsetY, offsetZ);
            
            vertexConsumer.vertex(vec3f.x, vec3f.y, vec3f.z).texture(u, v).color(red, green, blue, alpha).light(light).next();
        }
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
			return new AuraParticle(clientWorld, x, y, z, (float)velocityX, (float)velocityY, (float)velocityZ, this.spriteProvider);
		}
	}
}
