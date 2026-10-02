package walksy.customhitboxes.manager;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import walksy.customhitboxes.config.ConfigIntegration;
import walksy.customhitboxes.helper.WorldRenderHelper;
import walksy.customhitboxes.mixin.EntityInterpolationAccessor;

import java.awt.Color;

import static walksy.customhitboxes.CustomHitboxesMod.mc;

public final class HitboxRenderManager {
    public record HitboxState(Vec3 position, AABB box, Color top, Color bottom, float width,
                              boolean filled, float eyeHeight, Vec3 lookVector) {}

    public static HitboxState extract(Entity entity, EntityRenderState state, float partialTick) {
        if (!ConfigIntegration.CONFIG.instance().modEnabled
                || !mc.debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES)
                || entity.isInvisible() || !HitboxManager.shouldRender(entity)) return null;
        walksy.customhitboxes.CustomHitboxesMod.debugMessage("Rendering Entity: " + entity.getClass().getSimpleName());
        Vec3 position = new Vec3(state.x, state.y, state.z);
        if (HitboxManager.shouldRenderServerSide(entity) && entity != mc.player && entity instanceof LivingEntity) {
            // The interpolation target is the most recently received network position.
            // Unlike reading the integrated server, this works in multiplayer too.
            var target = ((EntityInterpolationAccessor) entity).customHitboxes$interpolationHandler().target();
            position = target != null ? target.position() : entity.position();
        }
        AABB box = entity.getBoundingBox().move(-entity.getX(), -entity.getY(), -entity.getZ());
        int fade = HitboxManager.hitboxMinus(entity);
        boolean filled = HitboxManager.isFilled(entity);
        Color top = fade(filled ? HitboxManager.getFilledColor(entity) : HitboxManager.getColor(entity), fade);
        Color bottom = filled ? top : fade(HitboxManager.getColorGradient(entity), fade);
        return new HitboxState(position, box, top, bottom, (float) HitboxManager.getLineThickness(entity), filled,
                entity.getEyeHeight(), HitboxManager.shouldRenderLookVector(entity) ? entity.getViewVector(partialTick).scale(2) : null);
    }

    private static Color fade(Color color, int fade) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, color.getAlpha() - fade));
    }

    public static void submit(HitboxState state, CameraRenderState camera, PoseStack poses, SubmitNodeCollector collector) {
        if (state == null) return;
        Vec3 relative = state.position().subtract(camera.pos);
        poses.pushPose();
        poses.translate(relative.x, relative.y, relative.z);
        WorldRenderHelper.renderBox(poses, collector, state.box(), state.top(), state.bottom(), state.width(), state.filled());
        if (state.lookVector() != null) WorldRenderHelper.drawVector(poses, collector, state.eyeHeight(), state.lookVector());
        poses.popPose();
    }
}
