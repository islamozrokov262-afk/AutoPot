package com.islamozroko.autopot;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AutoPotMod implements ClientModInitializer {
    private static KeyMapping potKey;
    private static int state = 0;
    private static int oldSlot = 0;
    
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("autopot", "main")
    );

    @Override
    public void onInitializeClient() {
        // Регистрируем клавишу с категорией (по умолчанию G)
        potKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.autopot.throw",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_G,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            if (state == 0 && potKey.consumeClick()) {
                int potSlot = findSplashPotion(client.player);
                if (potSlot != -1) {
                    oldSlot = client.player.getInventory().getSelectedSlot();
                    client.player.getInventory().setSelectedSlot(potSlot);
                    state = 1;
                }
            } 
            else if (state == 1) {
                client.options.keyUse.setDown(true);
                state = 2;
            }
            else if (state == 2) {
                client.options.keyUse.setDown(false);
                client.player.getInventory().setSelectedSlot(oldSlot);
                state = 0;
            }
        });
    }

    private int findSplashPotion(Player player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == Items.SPLASH_POTION) {
                return i;
            }
        }
        return -1;
    }
}
