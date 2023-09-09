package me.vbaddon.modules;

import me.vbaddon.VBAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class AutoChestSwap extends Module {
    int ticksOnGround = 1;
    public AutoChestSwap() {
        super(VBAddon.CATEGORY, "auto-chest-swap", "Automatically switches between chestplate and elytra.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        Item currentItem = mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem();

        if (ticksOnGround == 0) {
            equipElytra();
        } else if (ticksOnGround == 20) {
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
