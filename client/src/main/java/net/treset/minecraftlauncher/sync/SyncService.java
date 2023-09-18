package net.treset.minecraftlauncher.sync;

import javafx.util.Pair;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.util.FormatUtil;
import net.treset.minecraftlauncher.util.HttpUtil;

import java.io.IOException;
import java.util.List;

public class SyncService {
    private final String syncUrl;
    private final String syncPort;
    private final String syncKey;

    public SyncService(String syncUrl, String syncPort, String syncKey) {
        this.syncUrl = syncUrl;
        this.syncPort = syncPort;
        this.syncKey = syncKey;
    }

    public SyncService() {
        this(LauncherApplication.settings.getSyncUrl(), LauncherApplication.settings.getSyncPort(), LauncherApplication.settings.getSyncKey());
    }

    public void testConnection() throws IOException {
        get("/test");
    }

    public ComponentList getAvailable(String type) throws IOException {
        Pair<HttpUtil.HttpStatusCode, byte[]> result = get("/list/" + type);
        ComponentList response;
        try {
            response = ComponentList.fromJson(new String(result.getValue()));
        } catch(Exception e) {
            throw new IOException("Failed to parse the response from the server.\nError: " + e);
        }
        return response;
    }

    public void newComponent(String type, String id) throws IOException {
        get("/new/" + type + "/" + id);
    }

    public void uploadFile(String type, String id, String path, byte[] content) throws IOException {
        post("/file/" + type + "/" + id + "/" + FormatUtil.urlEncode(path), content);
    }

    public int complete(String type, String id) throws IOException {
        Pair<HttpUtil.HttpStatusCode, byte[]> result = get("/complete/" + type + "/" + id);
        return Integer.parseInt(new String(result.getValue()));
    }

    public GetResponse get(String type, String id, int version) throws IOException {
        Pair<HttpUtil.HttpStatusCode, byte[]> result = get("/get/" + type + "/" + id + "/" + version);
        return GetResponse.fromJson(new String(result.getValue()));
    }

    public byte[] downloadFile(String type, String id, String path) throws IOException {
        Pair<HttpUtil.HttpStatusCode, byte[]> result = get("/file/" + type + "/" + id + "/" + FormatUtil.urlEncode(path));
        return result.getKey() == HttpUtil.HttpStatusCode.NO_CONTENT ? new byte[]{} : result.getValue();
    }

    private Pair<HttpUtil.HttpStatusCode, byte[]> get(String route) throws IOException {
        Pair<HttpUtil.HttpStatusCode, byte[]> result;
        try {
            result = HttpUtil.get(this.syncUrl + ":" + this.syncPort + route, List.of(new Pair<>("Auth-Key", this.syncKey)));
        } catch(IOException e) {
            throw new IOException("Failed to connect to the sync server.\nError: " + e);
        }
        if(result.getKey().getCode() < 200 || result.getKey().getCode() >= 300) {
            throw new IOException("The server returned an error code.\nStatus: " + result.getKey() + (result.getValue() != null && result.getValue().length == 0 ? "\nMessage: " + new String(result.getValue()) : ""));
        }
        return result;
    }

    private Pair<HttpUtil.HttpStatusCode, byte[]> post(String route, byte[] data) throws IOException {
        Pair<HttpUtil.HttpStatusCode, byte[]> result;
        try {
            result = HttpUtil.post(this.syncUrl + ":" + this.syncPort + route, List.of(new Pair<>("Auth-Key", this.syncKey)), data);
        } catch(IOException e) {
            throw new IOException("Failed to connect to the sync server.\nError: " + e);
        }
        if(result.getKey().getCode() < 200 || result.getKey().getCode() >= 300) {
            throw new IOException("The server returned an error code.\nStatus: " + result.getKey() + (result.getValue() != null && result.getValue().length == 0 ? "\nMessage: " + new String(result.getValue()) : ""));
        }
        return result;
    }
}
