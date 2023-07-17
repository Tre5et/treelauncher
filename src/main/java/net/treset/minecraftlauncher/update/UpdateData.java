package net.treset.minecraftlauncher.update;

import com.google.gson.Gson;

import java.util.List;

public class UpdateData {
    private boolean available;
    private String version;
    private List<UpdateFile> files;

    public UpdateData(boolean available, String version, List<UpdateFile> files) {
        this.available = available;
        this.version = version;
        this.files = files;
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

    public static UpdateData fromJson(String json) {
        return new Gson().fromJson(json, UpdateData.class);
    }
}
