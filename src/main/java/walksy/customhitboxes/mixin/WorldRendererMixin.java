package walksy.customhitboxes.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import walksy.customhitboxes.config.ConfigIntegration;
import walksy.customhitboxes.manager.BlockOutlineRenderManager;

@Mixin(LevelRenderer.class)
public class WorldRendererMixin {
    @Inject(method = "submitBlockOutline", at = @At("HEAD"), cancellable = true)
    private void customHitboxes$outline(PoseStack poses, SubmitNodeCollector collector, LevelRenderState state, CallbackInfo ci) {
        if (!ConfigIntegration.CONFIG.instance().blockHoveringEnabled) return;
        BlockOutlineRenderState outline = state.blockOutlineRenderState;
        if (outline != null) {
            Vec3 camera = state.cameraRenderState.pos;
            poses.pushPose();
            poses.translate(outline.pos().getX() - camera.x, outline.pos().getY() - camera.y, outline.pos().getZ() - camera.z);
            BlockOutlineRenderManager.render(poses, collector, outline.shape());
            poses.popPose();
        }
        ci.cancel();
    }
}
