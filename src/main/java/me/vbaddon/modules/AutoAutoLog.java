package me.vbaddon.modules;

import me.vbaddon.VBAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AutoLog;
import meteordevelopment.orbit.EventHandler;

public class AutoAutoLog extends Module {
    long ticksAFK = 0;
    PlayerRotation playerRotation = new PlayerRotation();


    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> enable = sgGeneral.add(new BoolSetting.Builder()
        .name("enable")
        .description("Whether to enable AutoLog or not after going AFK.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> seconds = sgGeneral.add(new IntSetting.Builder()
            .name("seconds")
            .description("The amount of seconds being AFK before enabling AutoLog.")
            .range(5, 600)
            .sliderRange(5, 120)
            .defaultValue(20)
            .visible(enable::get)
            .build()
    );

    private final Setting<Boolean> disable = sgGeneral.add(new BoolSetting.Builder()
        .name("disable")
        .description("Whether to disable AutoLog or not.")
        .defaultValue(true)
        .build()
    );


    public AutoAutoLog() {
        super(VBAddon.CATEGORY, "auto-auto-log", "Automatically enables AutoLog module after being AFK for n seconds.");
    }

    @EventHandler
    private void onTick(TickEvent.Post e) {
        AutoLog autoLog = Modules.get().get(AutoLog.class);
        if (!playerRotation.hasChangedSinceLastCheck() && mc.player.getVelocity().horizontalLength() == 0)
            ticksAFK++;
        else ticksAFK = 0;


        if (enable.get() && ticksAFK / 20 == seconds.get() && !autoLog.isActive()){
            info("Enabled AutoLog after " + seconds.get() + " seconds of AFK!");
            autoLog.toggle();
        }

        if (disable.get() && ticksAFK == 0 && autoLog.isActive()) {
            info("Disabled AutoLog!");
            autoLog.toggle();
        }
    }

    private class PlayerRotation {
        float pitch;
        float roll;
        public PlayerRotation(float pitch, float roll) {
            this.pitch = pitch;
            this.roll = roll;
        }

        public PlayerRotation() {
            this.pitch = 0;
            this.roll = 0;
        }

        public boolean hasChangedSinceLastCheck() {
            boolean result;
            float pitchNow = mc.player.getPitch();
            float rollNow = mc.player.getRoll();

            result = pitch != pitchNow || roll != rollNow;
            this.pitch = pitchNow;
            this.roll = rollNow;
            return result;
        }
    }
}
