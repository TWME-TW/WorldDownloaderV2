package dev.twme.worldDownloaderV2;

import org.bukkit.plugin.java.JavaPlugin;

public final class WorldDownloaderV2 extends JavaPlugin {

    private ConfigManager configManager;
    private S3Manager s3Manager;

    @Override
    public void onEnable() {
        // initialize plugin
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        // initialize S3Manager
        s3Manager = new S3Manager(configManager);

        // register command
        this.getCommand("downloadworld").setExecutor(new DownloadWorldCommand(this, s3Manager, configManager));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
