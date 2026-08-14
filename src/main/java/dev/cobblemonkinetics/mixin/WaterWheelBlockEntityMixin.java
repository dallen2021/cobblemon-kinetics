package dev.cobblemonkinetics.mixin;

import com.simibubi.create.content.kinetics.waterwheel.WaterWheelBlockEntity;
import dev.cobblemonkinetics.config.CobblemonKineticsConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WaterWheelBlockEntity.class, remap = false)
abstract class WaterWheelBlockEntityMixin {

    @Inject(
        method = "determineAndApplyFlowScore",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void cobblemonKinetics$replaceNaturalWaterPower(CallbackInfo callback) {
        if (!CobblemonKineticsConfig.REPLACE_NATURAL_WATER_POWER.get()) {
            return;
        }
        WaterWheelBlockEntity wheel = (WaterWheelBlockEntity) (Object) this;
        wheel.setFlowScoreAndUpdate(0);
        callback.cancel();
    }
}
