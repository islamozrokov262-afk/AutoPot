package com.islamozroko.autopot;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;

public class AutoPotMod implements ClientModInitializer {
    private static KeyMapping potKey;
    
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("autopot", "main")
    );

    @Override
    public void onInitializeClient() {
        potKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.autopot.throw",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_G,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.getConnection() == null) return;

            while (potKey.consumeClick()) {
                // Ищем зелье скорости или силы
                int potSlot = findPotion(client.player);
                if (potSlot != -1) {
                    int oldSlot = client.player.getInventory().getSelectedSlot();
                    
                    // 1. Свапаем на зелье
                    client.player.getInventory().setSelectedSlot(potSlot);
                    
                    // 2. Мгновенно отправляем пакет использования (кидаем)
                    client.getConnection().send(new ServerboundUseItemPacket(
                            InteractionHand.MAIN_HAND, 
                            0, 
                            client.player.getYRot(), 
                            client.player.getXRot()
                    ));
                    
                    // 3. Мгновенно возвращаем меч
                    client.player.getInventory().setSelectedSlot(oldSlot);
                }
            }
        });
    }

    private int findPotion(Player player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == Items.SPLASH_POTION) {
                // Проверяем, что это зелье скорости или силы
                PotionContents contents = stack.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
                if (contents != null) {
                    String potionName = contents.potion().get().getRegisteredName();
                    if (potionName.contains("swiftness") || potionName.contains("strength")) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
}
