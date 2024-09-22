package dev.twme.worldDownloaderV2;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.progress.ProgressMonitor;

import java.io.File;

public class CompressionManager {

    public interface CompressionListener {
        void onProgress(int percent);

        void onComplete(File zipFile);

        void onError(Exception e);
    }

    public void compressWorld(File worldFolder, File outputZip, CompressionListener listener) {
        new Thread(() -> {
            try {
                ZipFile zipFile = new ZipFile(outputZip);
                zipFile.addFolder(worldFolder, new net.lingala.zip4j.model.ZipParameters());

                ProgressMonitor progressMonitor = zipFile.getProgressMonitor();

                while (progressMonitor.getState() == ProgressMonitor.State.BUSY) {
                    int percent = progressMonitor.getPercentDone();
                    listener.onProgress(percent);
                    Thread.sleep(500); // check every 0.5 seconds
                }

                if (progressMonitor.getResult() == ProgressMonitor.Result.SUCCESS) {
                    listener.onComplete(outputZip);
                } else {
                    listener.onError(new Exception("Compression failed"));
                }
            } catch (Exception e) {
                listener.onError(e);
            }
        }).start();
    }
}
