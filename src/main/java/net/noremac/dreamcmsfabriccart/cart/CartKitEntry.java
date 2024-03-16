package net.noremac.dreamcmsfabriccart.cart;

import net.minecraft.entity.player.PlayerEntity;
import net.noremac.dreamcmsfabriccart.DreamCMSFabricCart;
import net.noremac.dreamcmsfabriccart.DreamCMSPlayerCart;

public class CartKitEntry implements ICartEntry {
    private final int id;
    private final String kit;

    public CartKitEntry(int id, String kit) {
        this.id = id;
        this.kit = kit;
    }

    @Override
    public void receive(PlayerEntity player) {
        DreamCMSFabricCart.CART_INSTANCE.minecraftServer.getCommandManager().executeWithPrefix(
                DreamCMSFabricCart.CART_INSTANCE.minecraftServer.getCommandSource(), DreamCMSFabricCart.CART_INSTANCE.config.kitGiveCommand.replace("%player%", player.getName().getString()).replace("%kit%", this.kit)
        );
        DreamCMSFabricCart.CART_INSTANCE.pool.updateQuery("delete from cart_items where id=?", this.id);
    }

    @Override
    public String getLocalizedString() {
        return "§7[§fНабор ресурсов: §a" + this.kit + "§7]";
    }
}
