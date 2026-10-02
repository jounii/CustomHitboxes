package walksy.customhitboxes.manager;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.phys.shapes.VoxelShape;
import walksy.customhitboxes.config.ConfigIntegration;
import walksy.customhitboxes.helper.ColorHelper;
import walksy.customhitboxes.helper.WorldRenderHelper;

import java.awt.Color;

public final class BlockOutlineRenderManager {
    public static void render(PoseStack poses, SubmitNodeCollector collector, VoxelShape shape) {
        if (shape.isEmpty()) return;
        ConfigIntegration config = ConfigIntegration.CONFIG.instance();
        Color color = config.blockHoveringRainbow ? ColorHelper.getRainbowColor()
                : config.blockHoveringFilledHitbox ? config.blockHoveringFilledHitboxColor : config.blockHoveringColor;
        Color gradient = config.blockHoveringColorGradientEnabled && !config.blockHoveringRainbow
                ? config.blockHoveringColorGradient : color;
        WorldRenderHelper.renderBox(poses, collector, shape.bounds(), color, gradient,
                (float) config.blockHoveringBoxThickness, config.blockHoveringFilledHitbox);
    }
}
