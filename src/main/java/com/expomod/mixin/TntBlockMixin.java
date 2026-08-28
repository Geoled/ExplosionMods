package com.expomod.mixin;

import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TntBlock.class)
public abstract class TntBlockMixin {

    /**
     * Modify TNT to have realistic explosive power
     * Real TNT: ~4.6 MJ/kg, creates significant overpressure
     */
    @ModifyVariable(method = "explode", at = @At("HEAD"), argsOnly = true)
    private static float modifyTntPower(float power, Level level, BlockPos pos, BlockState state) {
        // Increase TNT power for more realistic explosion
        // Vanilla Minecraft TNT is too weak compared to real TNT
        return power * 1.5f; // 50% more powerful
    }

    @Inject(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)V"))
    private static void onTntExplode(Level level, BlockPos pos, BlockState state, CallbackInfo ci) {
        // Add realistic fuse behavior logging or effects
    }
}
