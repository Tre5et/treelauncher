package net.treset.minecraftlauncher.sync;

import javafx.util.Pair;
import net.treset.minecraftlauncher.LauncherApplication;
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
        Pair<Integer, String> result = HttpUtil.get(this.syncUrl + ":" + this.syncPort + "/test", List.of(new Pair<>("Auth-Key", this.syncKey)));
        if(result.getKey() != 200) {
            throw new IOException("The server returned an error code:\nStatusCode: " + result.getKey() + ", Message: " + result.getValue());
        }
    }
}
