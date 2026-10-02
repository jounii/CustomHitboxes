package walksy.customhitboxes.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InterpolationHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityInterpolationAccessor {
    @Accessor("interpolationHandler")
    InterpolationHandler customHitboxes$interpolationHandler();
}
