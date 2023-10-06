package net.treset.minecraftlauncher.update;

import net.treset.mc_version_loader.exception.FileDownloadException;
import net.treset.mc_version_loader.util.FileUtil;
import net.treset.minecraftlauncher.LauncherApplication;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.function.BiConsumer;

public class LauncherUpdater {
    private final String updateContent;
    private final UpdateData updateData;

    public LauncherUpdater() throws FileDownloadException {
        updateContent = FileUtil.getStringFromUrl(LauncherApplication.config.UPDATE_URL + LauncherApplication.stringLocalizer.get("launcher.version"));
        try {
            updateData = UpdateData.fromJson(updateContent);
        } catch (Exception e) {
            throw new FileDownloadException("Failed to parse update data", e);
        }
    }

    public String getUpdateVersion() {
        if(!updateData.isAvailable()) {
            return null;
        }
        return updateData.getVersion();
    }

    public int getFileCount() {
        return updateData.getFiles().stream().filter(file -> file.getUpdate() != null).toArray().length + (updateData.getUpdaterUrl() == null ? 0 : 1);
    }

    public void downloadFiles(BiConsumer<Integer, String> changeCallback) throws FileDownloadException {
        int count = 0;
        for(UpdateFile file : updateData.getFiles()) {
            if(file.getUpdate() == null) {
                continue;
            }
            changeCallback.accept(++count, file.getUpdate());
            URL url;
            try {
                url = new URL(file.getUrl());
            } catch (Exception e) {
                deleteUpdateFiles();
                throw new FileDownloadException("Failed to parse url=" + file.getUrl(), e);
            }
            try {
                FileUtil.downloadFile(url, new File(file.getUpdate() + ".update"));
            } catch (FileDownloadException e) {
                deleteUpdateFiles();
                throw e;
            }
        }
        if(updateData.getUpdaterUrl() != null) {
            changeCallback.accept(++count, "app/updater.jar");
            URL url;
            try {
                url = new URL(updateData.getUpdaterUrl());
            } catch (Exception e) {
                deleteUpdateFiles();
                throw new FileDownloadException("Failed to parse url=" + updateData.getUpdaterUrl(), e);
            }
            try {
                FileUtil.downloadFile(url, new File("app/updater.jar.update"));
            } catch (FileDownloadException e) {
                deleteUpdateFiles();
                throw e;
            }

            try {
                Files.delete(new File("app/updater.jar").toPath());
                Files.move(new File("app/updater.jar.update").toPath(), new File("app/updater.jar").toPath());
            } catch (IOException e) {
                deleteUpdateFiles();
                throw new FileDownloadException("Failed to move updater.jar.update to updater.jar", e);
            }
        }
    }

    public void writeFile() throws IOException {
        net.treset.minecraftlauncher.util.FileUtil.writeFile("update.json", updateContent);
    }

    public boolean deleteUpdateFiles() {
        boolean success = true;
        for(UpdateFile file : updateData.getFiles()) {
            File updateFile = new File(file.getUpdate() + ".update");
            if(updateFile.exists()) {
                if(!updateFile.delete()) {
                    success = false;
                }
            }
        }
        return success;
    }
    public UpdateData getUpdateData() {
        return updateData;
    }

    public String getUpdateInfo() {
        return updateData.getUpdateInfo();
    }
}
