package me.vbaddon.modules;

import me.vbaddon.VBAddon;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import java.util.Random;

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

    private final Setting<Boolean> ac = sgGeneral.add(new BoolSetting.Builder()
        .name("ac-compatibility")
        .description("Anticheat compatibility on karasique.")
        .defaultValue(true)
        .build()
    );

    public ElytraLimiter() {
        super(VBAddon.CATEGORY, "elytra-limiter", "Limits elytra speed to some value in block per second.");
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        Random random = new Random();
        double maxspeed = maxSpeed.get();
        double speed = event.movement.horizontalLength() * 20;
        if (!mc.player.isFallFlying() || speed <= maxspeed) return;
        double multiplier;
        if (ac.get())
            multiplier = maxspeed / speed + random.nextDouble() % 0.01 - 0.01;
        else
            multiplier = maxspeed / speed;
        ((IVec3d) event.movement).setXZ(event.movement.x * multiplier, event.movement.z * multiplier);
    }
    /*@EventHandler
    private void onTick(TickEvent.Pre event) {

        double maxspeed = maxSpeed.get();
        double speed = mc.player.getVelocity().horizontalLength() * 20;
        //info("speed: " + speed);
        if (!mc.player.isFallFlying() || speed <= maxspeed) return;
        double multiplier = maxspeed / speed;
        mc.player.setVelocity(mc.player.getVelocity().x * multiplier, mc.player.getVelocity().y, mc.player.getVelocity().z * multiplier);
    }*/
}
