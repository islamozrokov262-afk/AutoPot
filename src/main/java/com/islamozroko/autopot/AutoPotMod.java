package com.islamozroko.autopot;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AutoPotMod implements ClientModInitializer {
    private static KeyMapping potKey;
    
    // Слоты (0-8)
    private static final int STRENGTH_SLOT = 5;
    private static final int SPEED_SLOT = 6;
    
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("autopot", "main")
    );

    @Override
    public void onInitializeClient() {
        potKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.autopot.throw_all",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_G,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.getConnection() == null) return;

            while (potKey.consumeClick()) {
                int oldSlot = client.player.getInventory().getSelectedSlot();
                
                // 1. Кидаем СИЛУ (Слот 5)
                throwPotion(client, STRENGTH_SLOT);
                
                // 2. Кидаем СКОРОСТЬ (Слот 6)
                throwPotion(client, SPEED_SLOT);
                
                // 3. Возвращаем меч (без задержки)
                client.player.getInventory().setSelectedSlot(oldSlot);
                client.getConnection().send(new ServerboundSetCarriedItemPacket(oldSlot));
            }
        });
    }

    private void throwPotion(net.minecraft.client.Minecraft client, int slot) {
        Player player = client.player;
        if (player == null || client.getConnection() == null) return;
        
        ItemStack stack = player.getInventory().getItem(slot);
        if (stack.getItem() == Items.SPLASH_POTION) {
            // 1. Свапаем слот в инвентаре
            player.getInventory().setSelectedSlot(slot);
            // 2. Отправляем пакет смены слота (чтобы сервер знал, что мы взяли зелье)
            client.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
            
            // 3. Отправляем пакет броска (ПКМ)
            client.getConnection().send(new ServerboundUseItemPacket(
                    InteractionHand.MAIN_HAND, 
                    0, 
                    player.getYRot(), 
                    player.getXRot()
            ));
        }
    }
}
