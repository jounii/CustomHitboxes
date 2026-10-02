package walksy.customhitboxes;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import walksy.customhitboxes.config.ConfigIntegration;
import walksy.customhitboxes.manager.HitboxManager;



public class CustomHitboxesMod implements ModInitializer {

    public static Minecraft mc = Minecraft.getInstance();

    @Override
    public void onInitialize()
    {
        ConfigIntegration.CONFIG.load();

        //hooks
        HitboxManager.loadHook();
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> closeHook());
    }

    public static void closeHook()
    {
        //for some reason I'm forced to call the save procedure otherwise the config doesn't save
        //never had to do this before, probably something wrong with my config integration
        ConfigIntegration.CONFIG.save();
    }

    public static void debugMessage(Object message)
    {
        if (!ConfigIntegration.CONFIG.instance().debug) return;
        mc.showDebugChat(Component.literal(String.valueOf(message)));
    }
}
