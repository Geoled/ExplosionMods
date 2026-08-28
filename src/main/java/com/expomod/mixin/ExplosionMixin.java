package com.expomod.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {
    
    @Shadow public abstract Level getLevel();
    @Shadow public abstract double getX();
    @Shadow public abstract double getY();
    @Shadow public abstract double getZ();
    @Shadow public abstract float getSize();
    @Shadow public abstract boolean getFire();
    @Shadow public abstract Explosion.BlockInteraction getBlockInteraction();
    @Shadow public abstract List<Vec3> getToBlow();
    @Shadow public abstract Entity getSourceEntity();
    
    /**
     * Realistic explosion physics:
     * - Overpressure wave that decreases with distance squared (not linear)
     * - Fragment damage based on explosive type
     * - Realistic knockback with velocity-based calculations
     * - Heat and fire effects
     */
    @Inject(method = "explode", at = @At("HEAD"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void realisticExplosion(CallbackInfo ci) {
        Level level = getLevel();
        if (level.isClientSide()) {
            return; // Let client handle visual effects
        }
        
        double x = getX();
        double y = getY();
        double z = getZ();
        float basePower = getSize();
        
        // Calculate realistic blast radius using inverse square law
        // Real explosions have pressure that decreases with r^2
        float realisticRadius = calculateRealisticRadius(basePower);
        
        // Apply overpressure damage to entities
        applyOverpressureDamage(level, x, y, z, realisticRadius, basePower);
        
        // Apply realistic knockback based on impulse physics
        applyRealisticKnockback(level, x, y, z, realisticRadius, basePower);
        
        // Handle block destruction with realistic fragmentation
        destroyBlocksRealistically(level, x, y, z, realisticRadius);
        
        // Spawn particles and sounds
        spawnRealisticEffects(level, x, y, z, basePower);
        
        ci.cancel(); // Cancel vanilla explosion logic
    }
    
    private float calculateRealisticRadius(float basePower) {
        // TNT equivalent calculation
        // Real TNT has ~4.6 MJ/kg energy density
        // Minecraft TNT is roughly 1 block = ~1kg TNT equivalent
        return basePower * 2.5f; // More realistic radius multiplier
    }
    
    private void applyOverpressureDamage(Level level, double x, double y, double z, 
                                         float radius, float power) {
        List<Entity> entities = level.getEntities(null, 
            new net.minecraft.world.phys.AABB(
                x - radius, y - radius, z - radius,
                x + radius, y + radius, z + radius));
        
        for (Entity entity : entities) {
            double dist = entity.distanceToSqr(x, y, z);
            if (dist > radius * radius) continue;
            
            double actualDist = Math.sqrt(dist);
            
            // Overpressure decreases with square of distance (realistic)
            float overpressure = (power * power) / (float)(dist + 1.0);
            
            // Minimum threshold for damage
            if (overpressure < 0.1f) continue;
            
            // Calculate damage based on overpressure (in PSI equivalent)
            // 5 PSI can cause serious injury, 15+ PSI is usually fatal
            float damageMultiplier = Math.max(1.0f, overpressure * 2.0f);
            float damageAmount = power * damageMultiplier;
            
            entity.hurt(level.damageSources().explosion(null), damageAmount);
            
            // Chance to set on fire from explosion heat
            if (getFire() && level.random.nextFloat() < (power / (actualDist + 1))) {
                entity.setRemainingFireTicks((int)(power / actualDist * 20));
            }
        }
    }
    
    private void applyRealisticKnockback(Level level, double x, double y, double z,
                                         float radius, float power) {
        List<Entity> entities = level.getEntities(null,
            new net.minecraft.world.phys.AABB(
                x - radius, y - radius, z - radius,
                x + radius, y + radius, z + radius));
        
        for (Entity entity : entities) {
            double dist = entity.distanceToSqr(x, y, z);
            if (dist > radius * radius) continue;
            
            double actualDist = Math.sqrt(dist);
            if (actualDist < 0.1) actualDist = 0.1;
            
            // Impulse-based knockback (realistic physics)
            // Momentum transfer decreases with distance
            double impulse = (power * 15.0) / (actualDist * actualDist);
            
            Vec3 explosionCenter = new Vec3(x, y, z);
            Vec3 entityPos = entity.position();
            Vec3 direction = entityPos.subtract(explosionCenter).normalize();
            
            // Apply upward component for realistic arc
            double upwardBoost = Math.min(1.0, 3.0 / (actualDist + 1));
            
            entity.push(
                direction.x * impulse,
                Math.max(direction.y, upwardBoost) * impulse,
                direction.z * impulse
            );
        }
    }
    
    private void destroyBlocksRealistically(Level level, double x, double y, double z, float radius) {
        int intRadius = (int)Math.ceil(radius);
        
        for (int dx = -intRadius; dx <= intRadius; dx++) {
            for (int dy = -intRadius; dy <= intRadius; dy++) {
                for (int dz = -intRadius; dz <= intRadius; dz++) {
                    int bx = (int)x + dx;
                    int by = (int)y + dy;
                    int bz = (int)z + dz;
                    
                    double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
                    if (dist > radius) continue;
                    
                    // Block resistance affects destruction chance
                    var blockState = level.getBlockState(new net.minecraft.core.BlockPos(bx, by, bz));
                    float resistance = blockState.getBlock().getExplosionResistance();
                    
                    // Realistic: weaker blocks destroyed first, stronger blocks may survive
                    float destructionChance = (radius - (float)dist) / radius;
                    destructionChance *= (10.0f / (resistance + 1.0f));
                    
                    if (level.random.nextFloat() < destructionChance && resistance < 50.0f) {
                        level.destroyBlock(new net.minecraft.core.BlockPos(bx, by, bz), true);
                    }
                }
            }
        }
    }
    
    private void spawnRealisticEffects(Level level, double x, double y, double z, float power) {
        // Enhanced particle effects for realism
        int particleCount = (int)(power * 50);
        float radius = power * 2.0f;
        
        for (int i = 0; i < particleCount; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * radius * 2;
            double offsetY = (level.random.nextDouble() - 0.5) * radius * 2;
            double offsetZ = (level.random.nextDouble() - 0.5) * radius * 2;
            
            level.addParticle(
                net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
                x + offsetX, y + offsetY, z + offsetZ,
                0, 0, 0
            );
        }
        
        // Smoke particles
        for (int i = 0; i < particleCount / 2; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * radius;
            double offsetY = (level.random.nextDouble() - 0.5) * radius;
            double offsetZ = (level.random.nextDouble() - 0.5) * radius;
            
            level.addParticle(
                net.minecraft.core.particles.ParticleTypes.SMOKE,
                x + offsetX, y + offsetY, z + offsetZ,
                0, 0.1, 0
            );
        }
    }
}
