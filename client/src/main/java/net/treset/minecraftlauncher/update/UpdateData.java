package net.treset.minecraftlauncher.update;

import net.treset.mc_version_loader.json.GenericJsonParsable;

import java.util.List;

public class UpdateData extends GenericJsonParsable {
    private boolean available;
    private String version;
    private List<UpdateFile> files;
    private String updaterUrl;
    private String updateInfo;

    public UpdateData(boolean available, String version, List<UpdateFile> files, String updaterUrl) {
        this.available = available;
        this.version = version;
        this.files = files;
        this.updaterUrl = updaterUrl;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<UpdateFile> getFiles() {
        return files;
    }

    public void setFiles(List<UpdateFile> files) {
        this.files = files;
    }

    public String getUpdaterUrl() {
        return updaterUrl;
    }

    public void setUpdaterUrl(String updaterUrl) {
        this.updaterUrl = updaterUrl;
    }

    public String getUpdateInfo() {
        return updateInfo;
    }

    public void setUpdateInfo(String updateInfo) {
        this.updateInfo = updateInfo;
    }


    public static UpdateData fromJson(String json) {
        return fromJson(json, UpdateData.class);
    }
}
