package me.vbaddon.modules;

import me.vbaddon.VBAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class ElytraLimiter extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Double> maxSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("maximum-speed")
        .description("Maximum elytra speed (35 for karasique).")
        .defaultValue(35)
        .min(20)
        .sliderMax(40)
        .build()
    );
    public ElytraLimiter() {
        super(VBAddon.CATEGORY, "elytra-limiter", "Limits elytra speed to some value in block per second.");
    }

    /*@EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        double maxspeed = maxSpeed.get();
        double speed = event.movement.horizontalLength() * 20;
        if (!mc.player.isFallFlying() || speed <= maxspeed) return;
        double multiplier = maxspeed / speed;
        ((IVec3d) event.movement).setXZ(event.movement.x * multiplier, event.movement.z * multiplier);
    }*/
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        double maxspeed = maxSpeed.get();
        double speed = mc.player.getVelocity().horizontalLength() * 20;
        //info("speed: " + speed);
        if (!mc.player.isFallFlying() || speed <= maxspeed) return;
        double multiplier = maxspeed / speed;
        mc.player.setVelocity(mc.player.getVelocity().x * multiplier, mc.player.getVelocity().y, mc.player.getVelocity().z * multiplier);
    }
}
