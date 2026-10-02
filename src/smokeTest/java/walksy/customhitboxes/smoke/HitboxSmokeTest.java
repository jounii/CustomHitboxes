package walksy.customhitboxes.smoke;

import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.SimpleGizmoCollector;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PositionPath;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import walksy.customhitboxes.manager.HitboxManager;
import walksy.customhitboxes.mixin.EntityInterpolationAccessor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import walksy.customhitboxes.config.ConfigIntegration;
import walksy.customhitboxes.config.ModMenuIntegration;
import walksy.customhitboxes.config.setting.Settings;
import walksy.customhitboxes.manager.HitboxRenderManager;
import walksy.customhitboxes.manager.HitboxStateAccess;

import java.awt.Color;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class HitboxSmokeTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        context.runOnClient(client -> {
            ConfigIntegration config = ConfigIntegration.CONFIG.instance();
            client.debugEntries.setStatus(DebugScreenEntries.ENTITY_HITBOXES, DebugScreenEntryStatus.NEVER);
            config.modEnabled = true;
            config.blockHoveringFilledHitbox = false;
            config.blockHoveringRainbow = false;
            var settings = new LinkedHashMap<Settings.Type, Settings>();
            for (Settings.Type type : Settings.Type.values()) {
                Settings value = new Settings();
                value.shouldRender = true;
                value.color = new Color(0, 255, 0, 220);
                value.colorGradientEnabled = true;
                value.colorGradient = new Color(255, 0, 255, 220);
                value.boxThickness = 4;
                value.renderLookVector = true;
                settings.put(type, value);
            }
            config.profiles.put("26.3-smoke", settings);
            config.currentProfile = "26.3-smoke";
            config.team1 = new ArrayList<>(java.util.List.of("SmokeTeamMember"));
            config.team1Color = new Color(120, 30, 240, 160);
            config.blockHoveringEnabled = true;
            config.blockHoveringColor = new Color(255, 0, 0, 200);
            config.blockHoveringColorGradientEnabled = true;
            config.blockHoveringColorGradient = new Color(0, 0, 255, 200);
            config.blockHoveringBoxThickness = 5;
            ConfigIntegration.CONFIG.save();
            config.team1Color = Color.WHITE;
            ConfigIntegration.CONFIG.load();
            check(ConfigIntegration.CONFIG.instance().team1Color.equals(new Color(120, 30, 240, 160)), "RGBA team color round trip");
            check(ConfigIntegration.CONFIG.instance().getCurrentSettings().get(Settings.Type.HOSTILE_MOB).boxThickness == 4,
                    "profile settings round trip");
            try {
                var json = JsonParser.parseString(Files.readString(FabricLoader.getInstance().getConfigDir().resolve("walksycustomhitboxes.json"))).getAsJsonObject();
                check(json.has("profiles") && json.has("currentProfile") && json.has("team1"), "legacy configuration keys");
            } catch (java.io.IOException e) {
                throw new AssertionError("Saved configuration could not be read", e);
            }
            client.gui.setScreen(new ModMenuIntegration().getModConfigScreenFactory().create(null));
        });
        context.waitTicks(5);
        context.takeScreenshot("customhitboxes-config");
        context.clickScreenButton("Done");

        try (var world = context.worldBuilder().adjustSettings(settings -> {
            settings.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
            settings.setAllowCommands(true);
        }).create()) {
            world.getConnection().waitForChunksRender();
            var server = world.getServer();
            server.runCommand("fill -8 99 -8 8 99 8 minecraft:stone");
            server.runCommand("fill -8 100 -8 8 106 8 minecraft:air");
            server.runCommand("tp @a 0.5 100 5.5 180 0");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("summon minecraft:zombie 0.5 100 0.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Invulnerable:1b}");
            server.runCommand("summon minecraft:cow -2.5 100 0.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b,Invulnerable:1b}");
            server.runCommand("summon minecraft:end_crystal 3.5 100 0.5 {ShowBottom:0b}");
            server.runCommand("setblock 0 100 3 minecraft:stone");
            world.getConnection().waitForClientboundPackets();
            context.waitFor(client -> {
                for (Entity entity : client.level.entitiesForRendering()) {
                    if (entity.getType() == EntityTypes.ZOMBIE) return true;
                }
                return false;
            });
            context.runOnClient(client -> {
                check(client.debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES), "enabled-on-game-load debug flag");
                client.gui.hud.getChat().clearMessages(true);
            });
            context.waitTicks(20);
            context.runOnClient(client -> {
                Entity zombie = findZombie(client.level.entitiesForRendering());
                var state = client.getEntityRenderDispatcher().extractEntity(zombie, 0.5f);
                check(((HitboxStateAccess) state).customHitboxes$getState() != null, "hitbox state extraction");
                zombie.setInvisible(true);
                check(HitboxRenderManager.extract(zombie, state, 0.5f) == null, "invisible entity skipped");
                zombie.setInvisible(false);
                Settings settings = ConfigIntegration.CONFIG.instance().getCurrentSettings().get(Settings.Type.HOSTILE_MOB);
                settings.changeColorOnDamageTick = true;
                settings.damageTickColor = new Color(255, 20, 0, 180);
                ((LivingEntity) zombie).hurtTime = 2;
                var damaged = HitboxRenderManager.extract(zombie, state, 0.5f);
                check(damaged.top().equals(settings.damageTickColor) && damaged.bottom().equals(settings.damageTickColor), "hurt color overrides gradient");
                ((LivingEntity) zombie).hurtTime = 0;
                settings.changeColorOnDamageTick = false;
                settings.vanishWhenClose = true;
                settings.vanishFade = true;
                settings.vanishDistance = 100;
                check(HitboxRenderManager.extract(zombie, state, 0.5f).top().getAlpha() == 0, "close-distance alpha fading");
                settings.vanishWhenClose = false;
                settings.changeColorWhenClose = true;
                settings.closeDistance = 100;
                settings.closeColor = new Color(255, 200, 0, 160);
                check(HitboxRenderManager.extract(zombie, state, 0.5f).top().equals(settings.closeColor), "close-distance color transition");
                settings.changeColorWhenClose = false;
                settings.rainbow = true;
                check(HitboxRenderManager.extract(zombie, state, 0.5f).top().getAlpha() == 255, "rainbow color overrides configured color");
                settings.rainbow = false;
                settings.serverSideRender = true;
                var handler = ((EntityInterpolationAccessor) zombie).customHitboxes$interpolationHandler();
                Vec3 target = zombie.position().add(1, 0, 0);
                handler.interpolateTo(new PositionPath.Linear(target), 0, 0, false);
                check(HitboxRenderManager.extract(zombie, state, 0.5f).position().equals(target), "active network interpolation target");
                handler.cancel();
                settings.serverSideRender = false;
                var config = ConfigIntegration.CONFIG.instance();
                config.team1 = new ArrayList<>(java.util.List.of(client.player.getName().getString()));
                config.team1ColorGradientEnabled = false;
                config.recolorProjectiles = true;
                config.renderProjectiles = true;
                var pearl = new ThrownEnderpearl(EntityTypes.ENDER_PEARL, client.level);
                pearl.setOwner(client.player);
                check(HitboxManager.getColor(pearl).equals(config.team1Color), "team projectile color");
                check(HitboxManager.getColorGradient(pearl).equals(config.team1Color), "team projectile without gradient fallback");
                check(HitboxManager.shouldRender(pearl), "team projectile enabled");
                check(HitboxManager.getColor(client.player).equals(config.team1Color), "player team color");
            });
            context.runOnClient(client -> {
                check(collectVanillaHitboxes(client) == 0, "vanilla hitbox geometry suppressed when enabled");
                ConfigIntegration.CONFIG.instance().modEnabled = false;
                check(collectVanillaHitboxes(client) > 0, "vanilla hitbox geometry restored when disabled");
                ConfigIntegration.CONFIG.instance().modEnabled = true;
            });
            context.takeScreenshot("customhitboxes-outline-gradient");
            context.getInput().lookAt(new BlockPos(0, 100, 3));
            context.waitTicks(5);
            context.runOnClient(client -> check(client.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit
                    && hit.getBlockPos().equals(new BlockPos(0, 100, 3)), "targeted block selection"));
            context.takeScreenshot("customhitboxes-block-outline-width5");
            context.runOnClient(client -> {
                ConfigIntegration.CONFIG.instance().blockHoveringBoxThickness = 1;
                for (Settings settings : ConfigIntegration.CONFIG.instance().getCurrentSettings().values()) settings.boxThickness = 1;
            });
            context.waitTicks(5);
            context.takeScreenshot("customhitboxes-outline-width1");
            context.runOnClient(client -> {
                ConfigIntegration.CONFIG.instance().blockHoveringBoxThickness = 5;
                for (Settings settings : ConfigIntegration.CONFIG.instance().getCurrentSettings().values()) settings.boxThickness = 4;
            });
            context.getInput().holdKey(InputConstants.KEY_F3);
            context.getInput().pressKey(InputConstants.KEY_B);
            context.getInput().releaseKey(InputConstants.KEY_F3);
            context.waitTicks(3);
            context.runOnClient(client -> {
                Entity zombie = findZombie(client.level.entitiesForRendering());
                var state = client.getEntityRenderDispatcher().extractEntity(zombie, 0.5f);
                check(((HitboxStateAccess) state).customHitboxes$getState() == null, "F3+B off clears hitbox state");
            });
            context.takeScreenshot("customhitboxes-hitboxes-off");
            context.runOnClient(client -> {
                client.debugEntries.setStatus(DebugScreenEntries.ENTITY_HITBOXES, DebugScreenEntryStatus.ALWAYS_ON);
                for (Settings settings : ConfigIntegration.CONFIG.instance().getCurrentSettings().values()) {
                    settings.filledHitbox = true;
                    settings.filledHitboxColor = new Color(0, 160, 255, 80);
                    settings.serverSideRender = true;
                }
                ConfigIntegration.CONFIG.instance().blockHoveringFilledHitbox = true;
                ConfigIntegration.CONFIG.instance().blockHoveringFilledHitboxColor = new Color(255, 120, 0, 100);
            });
            context.waitTicks(10);
            context.takeScreenshot("customhitboxes-filled-server-position");
            context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_BACK));
            context.waitTicks(5);
            context.takeScreenshot("customhitboxes-third-person");
            server.runCommand("tp @a 1.5 100 5.5 180 0");
            world.getConnection().waitForClientboundPackets();
            context.waitTicks(10);
            context.runOnClient(client -> client.gui.hud.getChat().clearMessages(true));
            context.takeScreenshot("customhitboxes-camera-movement");
            context.runOnClient(client -> {
                ConfigIntegration.CONFIG.instance().modEnabled = false;
                client.options.setCameraType(CameraType.FIRST_PERSON);
                Entity zombie = findZombie(client.level.entitiesForRendering());
                var state = client.getEntityRenderDispatcher().extractEntity(zombie, 0.5f);
                check(((HitboxStateAccess) state).customHitboxes$getState() == null, "mod disabled clears custom state");
            });
            context.waitTicks(20);
            context.takeScreenshot("customhitboxes-vanilla-fallback");
        }
    }

    private static int collectVanillaHitboxes(Minecraft client) {
        SimpleGizmoCollector collector = new SimpleGizmoCollector();
        var camera = client.gameRenderer.mainCamera();
        Vec3 position = camera.position();
        try (var collection = Gizmos.withCollector(collector)) {
            new EntityHitboxDebugRenderer(client).emitGizmos(position.x, position.y, position.z,
                    client.getConnection().createDebugValueAccess(), camera.getCullFrustum(), 0.5f);
        }
        return collector.getGizmos().size();
    }

    private static Entity findZombie(Iterable<Entity> entities) {
        for (Entity entity : entities) if (entity.getType() == EntityTypes.ZOMBIE) return entity;
        throw new AssertionError("Test zombie was not loaded");
    }

    private static void check(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
        System.out.println("[CustomHitboxes smoke] PASS: " + description);
    }
}
