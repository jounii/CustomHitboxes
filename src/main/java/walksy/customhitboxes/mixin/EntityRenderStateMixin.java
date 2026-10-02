package walksy.customhitboxes.mixin;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import walksy.customhitboxes.manager.HitboxRenderManager.HitboxState;
import walksy.customhitboxes.manager.HitboxStateAccess;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements HitboxStateAccess {
    @Unique private HitboxState customHitboxes$state;

    public HitboxState customHitboxes$getState() { return customHitboxes$state; }
    public void customHitboxes$setState(HitboxState state) { customHitboxes$state = state; }
}
