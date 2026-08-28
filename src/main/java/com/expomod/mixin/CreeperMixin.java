package com.expomod.mixin;

import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Creeper.class)
public abstract class CreeperMixin {

    @Shadow public abstract void explodeCreeper();
    
    /**
     * Modify creeper explosion to be more realistic
     * Real military explosives are more powerful than vanilla creepers
     */
    @Inject(method = "explodeCreeper", at = @At("HEAD"), cancellable = true)
    private void realisticCreeperExplosion(CallbackInfo ci) {
        Level level = ((Creeper)(Object)this).level();
        
        if (level.isClientSide()) {
            return;
        }
        
        // Cancel vanilla explosion and use custom realistic one
        // Creeper explosion should be similar to small military charge
        double x = ((Creeper)(Object)this).getX();
        double y = ((Creeper)(Object)this).getY();
        double z = ((Creeper)(Object)this).getZ();
        
        // Create enhanced explosion with realistic effects
        level.explode(
            (Creeper)(Object)this,
            x,
            y,
            z,
            4.5f, // Increased from vanilla 3.0f for more realism
            true,
            Level.ExplosionInteraction.MOB
        );
        
        // The ExplosionMixin will handle the realistic physics
    }
}
