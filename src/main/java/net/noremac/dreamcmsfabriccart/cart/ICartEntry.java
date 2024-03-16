package net.noremac.dreamcmsfabriccart.cart;

import net.minecraft.entity.player.PlayerEntity;

public interface ICartEntry {
    void receive(PlayerEntity player);

    String getLocalizedString();
}
