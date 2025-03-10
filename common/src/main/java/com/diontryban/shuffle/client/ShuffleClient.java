/*
 * This file is part of Shuffle.
 * A copy of this program can be found at https://github.com/Trikzon/shuffle.
 * Copyright (C) 2023 Dion Tryban
 *
 * Shuffle is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * Shuffle is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Shuffle. If not, see <https://www.gnu.org/licenses/>.
 */

package com.diontryban.shuffle.client;

import com.diontryban.ash.api.client.event.ClientTickEvents;
import com.diontryban.ash.api.client.input.KeyMappingRegistry;
import com.diontryban.ash.api.event.UseBlockEvent;
import com.diontryban.shuffle.Shuffle;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShuffleClient {
    private static final Random RANDOM = new Random();
    private static final KeyMapping KEY = KeyMappingRegistry.registerKeyMapping(
            new ResourceLocation(Shuffle.MOD_ID, "shuffle"),
            GLFW.GLFW_KEY_R,
            Shuffle.MOD_ID
    );

    private static boolean shuffle = false;
    private static boolean keyWasDown = false;
    private static int slotToSwitchTo = -1;

    public static void init() {
        ClientTickEvents.registerStart(ShuffleClient::onClientStartTick);
        UseBlockEvent.register(ShuffleClient::onRightClickBlock);
    }

    private static void onClientStartTick(Minecraft client) {
        final LocalPlayer player = client.player;
        if (player == null) { return; }

        if (KEY.isDown() && !keyWasDown) {
            keyWasDown = true;

            shuffle = !shuffle;
            if (shuffle) {
                player.displayClientMessage(Component.translatable("message.shuffle.enable"), true);
                player.playSound(SoundEvents.TRIPWIRE_CLICK_OFF, 0.5f, 1.0f);
            } else {
                player.displayClientMessage(Component.translatable("message.shuffle.disable"), true);
                player.playSound(SoundEvents.TRIPWIRE_CLICK_ON, 0.5f, 1.0f);
            }
        } else if (!KEY.isDown() && keyWasDown) {
            keyWasDown = false;
        }

        if (slotToSwitchTo >= 0 && slotToSwitchTo <= 8) {
            player.getInventory().selected = slotToSwitchTo;
            slotToSwitchTo = -1;
        }
    }

    private static InteractionResult onRightClickBlock(
            Player player,
            Level level,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (shuffle && level.isClientSide && !player.isSpectator()) {
            final Item itemInHand = player.getItemInHand(hand).getItem();
            // Only shuffle if the held item is a block, therefore it's being placed.
            if (Block.byItem(itemInHand) != Blocks.AIR) {
                // Collect the slot ids that contain BlockItems.
                final List<Integer> slotsWithBlocks = new ArrayList<>();
                final NonNullList<ItemStack> items = player.getInventory().items;

                for (int i = 0; i <= 8; i++) {
                    final Item item = items.get(i).getItem();
                    if (Block.byItem(item) != Blocks.AIR) {
                        slotsWithBlocks.add(i);
                    }
                }

                if (slotsWithBlocks.size() > 0) {
                    slotToSwitchTo = slotsWithBlocks.get(RANDOM.nextInt(slotsWithBlocks.size()));
                }
            }
        }
        return InteractionResult.PASS;
    }
}
