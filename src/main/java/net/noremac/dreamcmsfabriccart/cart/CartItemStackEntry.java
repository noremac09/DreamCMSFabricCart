package net.noremac.dreamcmsfabriccart.cart;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.noremac.dreamcmsfabriccart.DreamCMSFabricCart;

public class CartItemStackEntry implements ICartEntry {

    private final int id;
    private final String type;
    private final int damage;
    private final int count;
    private final String enchants;
    private final String nbt;

    private final ItemStack itemStack;

    public CartItemStackEntry(int id, String type, int damage, int count, String enchants, String nbt) {

        this.id = id;
        this.type = type;
        this.damage = damage;
        this.count = count;
        this.enchants = enchants;
        this.nbt = nbt;

        Item item;
        try {
            item = Registries.ITEM.get(Identifier.fromCommandInput(new StringReader(type)));
            this.itemStack = new ItemStack(item, count);
            this.itemStack.setDamage(damage);

            if (this.nbt != null) {
                this.itemStack.setNbt(StringNbtReader.parse(this.nbt));
            }

        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void receive(PlayerEntity player) {
        player.giveItemStack(this.itemStack.copy());
        DreamCMSFabricCart.CART_INSTANCE.pool.updateQuery("delete from cart_items where id=?", this.id);
    }

    @Override
    public String getLocalizedString() {
        return "§7[§e" + this.itemStack.getName().getString() + " §8- §a" + this.itemStack.getCount() + "шт.§7]";
    }
}
