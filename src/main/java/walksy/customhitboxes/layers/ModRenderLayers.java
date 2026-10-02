package walksy.customhitboxes.layers;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public final class ModRenderLayers {
    // Vanilla RenderPearl pipelines provide alpha blending, depth testing, no culling,
    // and OIT variants. Line width is now a vertex attribute, not mutable GL state.
    public static RenderType filled() {
        return RenderTypes.debugQuads();
    }

    public static RenderType outlined() {
        return RenderTypes.linesTranslucent();
    }
}
