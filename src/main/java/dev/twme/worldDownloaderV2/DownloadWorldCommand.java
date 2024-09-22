package dev.twme.worldDownloaderV2;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DownloadWorldCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final S3Manager s3Manager;
    private final ConfigManager configManager;
    private final CompressionManager compressionManager;

    public DownloadWorldCommand(JavaPlugin plugin, S3Manager s3Manager, ConfigManager configManager) {
        this.plugin = plugin;
        this.s3Manager = s3Manager;
        this.configManager = configManager;
        this.compressionManager = new CompressionManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(configManager.getMessage("senderNotPlayer"));
            return true;
        }

        String messageStart = configManager.getMessage("startCompression");
        player.sendMessage(messageStart);

        // get world folder
        File worldFolder = player.getWorld().getWorldFolder();
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        File zipFile = new File(plugin.getDataFolder(), worldFolder.getName() + "_" + timestamp + ".zip");

        // zip world folder
        compressionManager.compressWorld(worldFolder, zipFile, new CompressionManager.CompressionListener() {
            @Override
            public void onProgress(int percent) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    String msg = configManager.getMessage("compressionProgress").replace("%s", String.valueOf(percent));
                    player.sendMessage(msg);
                });
            }

            @Override
            public void onComplete(File zipped) {
                Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(configManager.getMessage("compressionComplete")));

                // 上傳至 S3
                s3Manager.uploadFile(zipped, zipped.getName()).thenAccept(url -> {
                    if (url != null) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            String downloadMsg = configManager.getMessage("downloadLink").replace("%s", url);
                            player.sendMessage(downloadMsg);
                            // 刪除本地壓縮文件
                            zipped.delete();
                        });
                    } else {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            String errorMsg = configManager.getMessage("error").replace("%s", configManager.getMessage("uploadFailed"));
                            player.sendMessage(errorMsg);
                        });
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    String errorMsg = configManager.getMessage("error").replace("%s", e.getMessage());
                    player.sendMessage(errorMsg);
                });
            }
        });

        return true;
    }
}
