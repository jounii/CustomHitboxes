package walksy.customhitboxes.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import walksy.customhitboxes.manager.HitboxRenderManager;
import walksy.customhitboxes.manager.HitboxStateAccess;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Inject(method = "extractEntity", at = @At("RETURN"))
    private void customHitboxes$extract(Entity entity, float partialTick, CallbackInfoReturnable<EntityRenderState> cir) {
        EntityRenderState state = cir.getReturnValue();
        ((HitboxStateAccess) state).customHitboxes$setState(HitboxRenderManager.extract(entity, state, partialTick));
    }

    @Inject(method = "submit", at = @At("TAIL"))
    private void customHitboxes$submit(EntityRenderState state, CameraRenderState camera, double x, double y, double z,
                                      PoseStack poses, SubmitNodeCollector collector, CallbackInfo ci) {
        HitboxRenderManager.submit(((HitboxStateAccess) state).customHitboxes$getState(), camera, poses, collector);
    }
}
