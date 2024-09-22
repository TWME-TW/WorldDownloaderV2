package dev.twme.worldDownloaderV2;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }

    public String getS3Endpoint() {
        return config.getString("s3.endpoint");
    }

    public String getS3AccessKey() {
        return config.getString("s3.accessKey");
    }

    public String getS3SecretKey() {
        return config.getString("s3.secretKey");
    }

    public String getS3Bucket() {
        return config.getString("s3.bucket");
    }

    public int getLinkExpiry() {
        return config.getInt("linkExpiry"); // 單位：秒
    }

    public String getMessage(String key) {
        return config.getString("messages." + key);
    }
}