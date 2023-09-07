package me.vbaddon;

import me.vbaddon.modules.BetterTabPlus;
import me.vbaddon.modules.ElytraLimiter;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class VBAddon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("VB Mods");

    @Override
    public void onInitialize() {
        LOG.info("Initializing VB Addon!");

        // Modules
        Modules.get().add(new ElytraLimiter());
        //Modules.get().add(new BetterTabPlus());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "me.vbaddon";
    }
}
