package com.hrens;

import com.hrens.commands.*;
import com.hrens.listener.PlayerEvent;
import com.hrens.utils.BanManager;
import com.hrens.utils.LogManager;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import lombok.SneakyThrows;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class BungeeSystem extends Plugin {
    public boolean module_bansystem;
    Configuration messageConfig;
    MongoClient mongoClient;
    public static BungeeSystem instance;
    private LogManager logManager;
    private BanManager banManager;
    Configuration config;
    String prefix;
    MongoDatabase mongodatabase;
    @Override
    public void onEnable() {
        getLogger().info("Loading ServerSystem...");
        instance = this;
        getLogger().info("Loading Config...");
        try {
            makeConfig();
            makeMessageConfig();
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
            messageConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "messages.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        getLogger().info("Config loaded");
        prefix = config.getString("prefix");
        getLogger().info("Connecting to Database");
        module_bansystem = getConfig().getBoolean("modules.bansystem.enabled");
        mongoClient = MongoClients.create(Objects.requireNonNull(config.getString("mongodb.mongourl")));
        mongodatabase = mongoClient.getDatabase(Objects.requireNonNull(config.getString("mongodb.database")));
        logManager = new LogManager();
        banManager = new BanManager();
        PluginManager pluginManager = getProxy().getPluginManager();
        pluginManager.registerCommand(this, new BanCommand("ban", "" ));
        pluginManager.registerCommand(this, new UnbanCommand("unban", "bungeesystem.unban"));
        pluginManager.registerCommand(this, new MuteCommand("mute", ""));
        pluginManager.registerCommand(this, new UnmuteCommand("unmute", "bungeesystem.unmute"));
        pluginManager.registerCommand(this, new CheckCommand("check", "bungeesystem.check"));
        pluginManager.registerCommand(this, new RebootCommand("reboot", "bungeesystem.reboot"));
        pluginManager.registerListener(this, new PlayerEvent());
        getLogger().info("Loaded Serversystem");
    }

    public MongoDatabase getMongodatabase() {
        return mongodatabase;
    }

    public Configuration getConfig() {
        return config;
    }

    public static BungeeSystem getInstance() {
        return instance;
    }

    public void makeConfig() throws IOException {
        if (!getDataFolder().exists()) {
            getLogger().info("Created config folder: " + getDataFolder().mkdir());
        }

        File configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            FileOutputStream outputStream = new FileOutputStream(configFile);
            InputStream in = getResourceAsStream("config.yml");
            in.transferTo(outputStream);
        }
    }
    public void makeMessageConfig() throws IOException {
        if (!getDataFolder().exists()) {
            getLogger().info("Created config folder: " + getDataFolder().mkdir());
        }

        File configFile = new File(getDataFolder(), "messages.yml");

        if (!configFile.exists()) {
            FileOutputStream outputStream = new FileOutputStream(configFile);
            InputStream in = getResourceAsStream("messages.yml");
            in.transferTo(outputStream);
        }
    }
    public String getMessageString(String path){
        return messageConfig.getString(path)
                .replace("{prefix}", prefix)
                .replace("{\\n}", System.lineSeparator());
    }
    public BaseComponent[] getMessage(String path){
        return TextComponent.fromLegacyText(messageConfig.getString(path)
                .replace("{prefix}", prefix)
                .replaceAll("\\n", System.lineSeparator()));
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public BanManager getBanManager() {
        return banManager;
    }
}