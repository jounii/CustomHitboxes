package walksy.customhitboxes.manager;

import walksy.customhitboxes.manager.HitboxRenderManager.HitboxState;

public interface HitboxStateAccess {
    HitboxState customHitboxes$getState();
    void customHitboxes$setState(HitboxState state);
}
