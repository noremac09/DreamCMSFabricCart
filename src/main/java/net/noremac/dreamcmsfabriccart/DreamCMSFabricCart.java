package net.noremac.dreamcmsfabriccart;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.StringReader;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.noremac.dreamcmsfabriccart.cart.ICartEntry;
import net.noremac.dreamcmsfabriccart.db.MySQLConnectionPool;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static net.minecraft.server.command.CommandManager.literal;

public class DreamCMSFabricCart implements ModInitializer {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final String CONFIG_DIRECTORY = "DreamCMS";
    public DreamCMSCartConfig config;
    public MySQLConnectionPool pool;
    public MinecraftServer minecraftServer;

    public static DreamCMSFabricCart CART_INSTANCE;

    @Override
    public void onInitialize() {
        CART_INSTANCE = this;
        this.config = this.initConfig();

        try {
            this.pool = new MySQLConnectionPool(this.config.hostname + ":" + this.config.port, this.config.username, this.config.password, this.config.database);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        ServerLifecycleEvents.SERVER_STARTED.register(srv -> this.minecraftServer = srv);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                        literal("cart")
                                .executes(context -> {
                                    context.getSource().sendFeedback(() -> Text.literal("§a[Корзина сервера]"), false);
                                    context.getSource().sendFeedback(() -> Text.literal("§7/cart list §8- §fузнать какие предметы в корзине"), false);
                                    context.getSource().sendFeedback(() -> Text.literal("§7/cart receive §8- §fполучить все предметы из корзины"), false);
                                    return 1;
                                })
                                .then(literal("list")
                                        .executes(context -> {
                                            PlayerEntity player = context.getSource().getPlayer();
                                            DreamCMSPlayerCart dreamCMSPlayerCart = new DreamCMSPlayerCart(player);
                                            if (dreamCMSPlayerCart.storage.isEmpty()) {
                                                context.getSource().sendFeedback(() -> Text.literal("§cВаша корзина пуста, вы можете купить предметы на сайте."), false);
                                                return 1;
                                            }
                                            context.getSource().sendFeedback(() -> Text.literal("§eСодержимое корзины:"), false);
                                            for (ICartEntry iCartEntry : dreamCMSPlayerCart.storage) {
                                                context.getSource().sendFeedback(() -> Text.literal("§a- " + iCartEntry.getLocalizedString()), false);
                                            }
                                            return 1;
                                        })
                                )
                                .then(literal("receive")
                                        .executes(context -> {
                                            PlayerEntity player = context.getSource().getPlayer();
                                            DreamCMSPlayerCart dreamCMSPlayerCart = new DreamCMSPlayerCart(player);
                                            if (dreamCMSPlayerCart.storage.isEmpty()) {
                                                context.getSource().sendFeedback(() -> Text.literal("Ваша корзина пуста, вы можете купить предметы на сайте."), false);
                                                return 1;
                                            }
                                            for (ICartEntry entry : dreamCMSPlayerCart.storage) {
                                                entry.receive(player);
                                                context.getSource().sendFeedback(() -> Text.literal("§7Выдан предмет: §a" + entry.getLocalizedString()), false);
                                            }
                                            return 1;
                                        })
                                )
                )
        );

    }

    private DreamCMSCartConfig initConfig() {
        File configDir = new File("config/" + CONFIG_DIRECTORY);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        File configFile = new File(configDir, "config.json");
        if (!configFile.exists()) {
            DreamCMSCartConfig dreamCMSFabricCart = new DreamCMSCartConfig();
            try {
                FileUtils.writeStringToFile(configFile, GSON.toJson(dreamCMSFabricCart), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            return GSON.fromJson(FileUtils.readFileToString(configFile, StandardCharsets.UTF_8), DreamCMSCartConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
