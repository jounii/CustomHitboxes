package walksy.customhitboxes.mixin;

import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.customhitboxes.config.ConfigIntegration;

@Mixin(EntityHitboxDebugRenderer.class)
public class EntityHitboxDebugRendererMixin {
    @Inject(method = "showHitboxes", at = @At("HEAD"), cancellable = true)
    private void customHitboxes$replaceVanilla(Entity entity, float partialTick, boolean serverSide, CallbackInfo ci) {
        if (ConfigIntegration.CONFIG.instance().modEnabled) ci.cancel();
    }
}
