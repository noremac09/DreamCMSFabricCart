package net.noremac.dreamcmsfabriccart;

import net.minecraft.entity.player.PlayerEntity;
import net.noremac.dreamcmsfabriccart.cart.CartItemStackEntry;
import net.noremac.dreamcmsfabriccart.cart.CartKitEntry;
import net.noremac.dreamcmsfabriccart.cart.ICartEntry;
import net.noremac.dreamcmsfabriccart.db.MySQLConnectionPool;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DreamCMSPlayerCart {

    public List<ICartEntry> storage = new ArrayList<>();

    public DreamCMSPlayerCart(PlayerEntity player) {
        ResultSet resultSet = DreamCMSFabricCart.CART_INSTANCE.pool.selectQuery("select * from cart_items where `uuid`=?", player.getUuid());
        while (true) {
            try {
                if (!resultSet.next()) break;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            try {
                int id = resultSet.getInt("id");
                int damage = resultSet.getInt("damage");
                int count = resultSet.getInt("count");
                String enchants = resultSet.getString("enchants");
                String nbt = resultSet.getString("nbt");
                String type = resultSet.getString("type");
                ICartEntry entry = switch (type) {
                    case "ESSENTIALS_KIT" -> new CartKitEntry(id, nbt);
                    default -> new CartItemStackEntry(id, type, damage, count, enchants, nbt);
                };
                storage.add(entry);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
