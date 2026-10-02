package walksy.customhitboxes.helper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import walksy.customhitboxes.layers.ModRenderLayers;

import java.awt.Color;

public final class WorldRenderHelper {
    private static final int[][] EDGES = {
            {0, 1}, {1, 3}, {3, 2}, {2, 0},
            {4, 5}, {5, 7}, {7, 6}, {6, 4},
            {0, 4}, {1, 5}, {2, 6}, {3, 7}
    };
    private static final int[][] FACES = {
            {0, 1, 3, 2}, {4, 6, 7, 5}, {0, 4, 5, 1},
            {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 5, 7, 3}
    };

    public static void renderBox(PoseStack poses, SubmitNodeCollector collector, AABB box,
                                 Color top, Color bottom, float width, boolean filled) {
        if (top.getAlpha() == 0 && bottom.getAlpha() == 0) return;
        if (!filled && (!Float.isFinite(width) || width <= 0)) return;
        // Capture values only: custom geometry executes later on the render thread.
        collector.submitCustomGeometry(poses, filled ? ModRenderLayers.filled() : ModRenderLayers.outlined(),
                (pose, vertices) -> {
                    Vec3[] points = new Vec3[8];
                    for (int i = 0; i < 8; i++) {
                        points[i] = new Vec3((i & 1) == 0 ? box.minX : box.maxX,
                                (i & 4) == 0 ? box.minY : box.maxY,
                                (i & 2) == 0 ? box.minZ : box.maxZ);
                    }
                    if (filled) {
                        for (int[] face : FACES) {
                            for (int i : face) {
                                vertex(pose, vertices, points[i], (i & 4) == 0 ? bottom : top);
                            }
                        }
                    } else {
                        for (int[] edge : EDGES) {
                            line(pose, vertices, points[edge[0]], points[edge[1]],
                                    (edge[0] & 4) == 0 ? bottom : top,
                                    (edge[1] & 4) == 0 ? bottom : top, width);
                        }
                    }
                });
    }

    public static void drawVector(PoseStack poses, SubmitNodeCollector collector, float eyeHeight, Vec3 vector) {
        Vec3 start = new Vec3(0, eyeHeight, 0);
        collector.submitCustomGeometry(poses, ModRenderLayers.outlined(),
                (pose, vertices) -> line(pose, vertices, start, start.add(vector), Color.BLUE, Color.BLUE, 1));
    }

    private static VertexConsumer vertex(PoseStack.Pose pose, VertexConsumer vertices, Vec3 point, Color color) {
        return vertices.addVertex(pose, (float) point.x, (float) point.y, (float) point.z).setColor(color.getRGB());
    }

    private static void line(PoseStack.Pose pose, VertexConsumer vertices, Vec3 start, Vec3 end,
                             Color startColor, Color endColor, float width) {
        Vec3 normal = end.subtract(start).normalize();
        vertex(pose, vertices, start, startColor)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z).setLineWidth(width);
        vertex(pose, vertices, end, endColor)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z).setLineWidth(width);
    }
}
