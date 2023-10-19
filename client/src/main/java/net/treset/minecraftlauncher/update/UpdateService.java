package net.treset.minecraftlauncher.update;

import javafx.util.Pair;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.util.HttpService;
import net.treset.minecraftlauncher.util.HttpUtil;

import java.io.IOException;

public class UpdateService extends HttpService {
    public UpdateService() {
        super(LauncherApplication.config.UPDATE_URL);
    }

    public Update update() throws IOException {
        Pair<HttpUtil.HttpStatusCode, byte[]> result = get("update/" + LauncherApplication.stringLocalizer.get("launcher.version"));
        String response = new String(result.getValue());
        try {
            return Update.fromJson(response);
        } catch(Exception e) {
            throw new IOException("Failed to parse the response from the server.\nError: " + e);
        }
    }

    public byte[] file(String version, String file) throws IOException {
        Pair<HttpUtil.HttpStatusCode, byte[]> result = get("file/" + version + "/" + file);
        return result.getValue();
    }
}
