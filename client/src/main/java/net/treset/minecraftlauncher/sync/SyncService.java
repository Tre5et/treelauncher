package net.treset.minecraftlauncher.sync;

import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.util.FormatUtil;
import net.treset.minecraftlauncher.util.HttpService;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SyncService extends HttpService {
    public SyncService(String syncUrl, String syncPort, String syncKey) {
        super("http://" + syncUrl + ":" + syncPort, List.of(new Pair<>("Auth-Key", syncKey)));
    }

    public SyncService() {
        this(LauncherApplication.settings.getSyncUrl(), LauncherApplication.settings.getSyncPort(), LauncherApplication.settings.getSyncKey());
    }

    public static boolean isSyncing(LauncherManifest manifest) {
        return new File(FormatUtil.absoluteFilePath(manifest.getDirectory(), LauncherApplication.config.SYNC_FILENAME)).isFile();
    }

    public static String convertType(LauncherManifestType type) throws IOException {
        return switch (type) {
            case RESOURCEPACKS_COMPONENT -> "resourcepacks";
            case MODS_COMPONENT -> "mods";
            case OPTIONS_COMPONENT -> "options";
            case SAVES_COMPONENT -> "saves";
            case INSTANCE_COMPONENT -> "instance";
            default -> throw new IOException("Invalid type: " + type);
        };
    }

    public void testConnection() throws IOException {
        get("/test");
    }

    public ComponentList getAvailable(String type) throws IOException {
        Pair<HttpStatusCode, byte[]> result = get("/list/" + type);
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
        Pair<HttpStatusCode, byte[]> result = get("/complete/" + type + "/" + id);
        return Integer.parseInt(new String(result.getValue()));
    }

    public GetResponse get(String type, String id, int version) throws IOException {
        Pair<HttpStatusCode, byte[]> result = get("/get/" + type + "/" + id + "/" + version);
        return GetResponse.fromJson(new String(result.getValue()));
    }

    public byte[] downloadFile(String type, String id, String path) throws IOException {
        Pair<HttpStatusCode, byte[]> result = get("/file/" + type + "/" + id + "/" + FormatUtil.urlEncode(path));
        return result.getKey() == HttpStatusCode.NO_CONTENT ? new byte[]{} : result.getValue();
    }
}
