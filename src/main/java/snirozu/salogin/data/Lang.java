package snirozu.salogin.data;

import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import snirozu.salogin.Salogin;

public class Lang {
    public static FileConfiguration langConfig;

    public static String local(String path) {
        return ChatColor.translateAlternateColorCodes('&', langConfig.getString(path));
    }

    public static String local(String path, Object... placeholders) {
        return ChatColor.translateAlternateColorCodes('&', langConfig.getString(path).formatted(placeholders));
    }

    public static void send(CommandSender sender, String path) {
        sender.sendMessage(local(path));
    }

    public static void send(CommandSender sender, String path, Object... placeholders) {
        sender.sendMessage(local(path, placeholders));
    }

    private static File _langConfigFile;
    
    public static void initLangConfig() {
        _langConfigFile = new File(Salogin.instance.getDataFolder().toString() + "/messages/", Salogin.instance.getConfig().getString("language") + ".yml");
        if (!_langConfigFile.exists()) {
            updateConfig(true);
        }
        loadConfig();
    }

    public static void updateConfig(boolean replace) {
        _langConfigFile.getParentFile().mkdirs();
        Salogin.instance.saveResource("messages/" + Salogin.instance.getConfig().getString("language") + ".yml", replace);
    }

    public static void loadConfig() {
        langConfig = new YamlConfiguration();
        try {
            langConfig.load(_langConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}
