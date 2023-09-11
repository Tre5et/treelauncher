package net.treset.minecraftlauncher.update;

public class UpdateFile {
    private String remove;
    private String update;
    private String url;

    public UpdateFile(String remove, String update, String url) {
        this.remove = remove;
        this.update = update;
        this.url = url;
    }

    public String getRemove() {
        return remove;
    }

    public void setRemove(String remove) {
        this.remove = remove;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return remove + " -> " + update;
    }
}
