package me.vbaddon.modules;

import me.vbaddon.VBAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class AutoChestSwap extends Module {
    int ticksOnGround = 1;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public AutoChestSwap() {
        super(VBAddon.CATEGORY, "auto-chest-swap", "Automatically switches between chestplate and elytra.");
    }

    private final Setting<Boolean> firework = sgGeneral.add(new BoolSetting.Builder()
        .name("only-when-holding-firework")
        .description("You have to hold firework in hand to switch to elytra.")
        .defaultValue(false)
        .build()
    );

    @EventHandler
    private void onTick(TickEvent.Post event) {
        Item currentItem = mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem();

        if (ticksOnGround == 0 && currentItem != Items.ELYTRA) {
            if (!firework.get()) equipElytra();
            if (firework.get() && mc.player.getMainHandStack().getItem() == Items.FIREWORK_ROCKET)
                equipElytra();
        } else if (ticksOnGround == 20 && !(currentItem instanceof ArmorItem && ((ArmorItem) currentItem).getSlotType() == EquipmentSlot.CHEST)) { //unreadable
            equipChestplate();
        }

        if (mc.player.isOnGround()) ticksOnGround++;
        else ticksOnGround = 0;
    }

    private boolean equipElytra() {
        boolean result = false;

        for (int i = 0; i < mc.player.getInventory().main.size(); i++) {
            Item item = mc.player.getInventory().main.get(i).getItem();

            if (item == Items.ELYTRA) {
                InvUtils.move().from(i).toArmor(2);
                result = true;
                break;
            }
        }
        return result;
    }

    private boolean equipChestplate() {
        int bestSlot = -1;

        for (int i = 0; i < mc.player.getInventory().main.size(); i++) {
            Item item = mc.player.getInventory().main.get(i).getItem();

            if (item == Items.DIAMOND_CHESTPLATE) {
                bestSlot = i;
            } else if (item == Items.NETHERITE_CHESTPLATE) {
                bestSlot = i;
                break;
            }
        }

        if (bestSlot != -1) InvUtils.move().from(bestSlot).toArmor(2);;
        return bestSlot != -1;
    }
}
