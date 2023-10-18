package net.treset.minecraftlauncher.update;

import javafx.util.Pair;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.util.HttpUtil;

import java.io.IOException;
import java.util.List;

public class UpdateService {
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

    public String news() throws IOException {
        Pair<HttpUtil.HttpStatusCode, byte[]> result = get("news/" + LauncherApplication.stringLocalizer.get("launcher.version"));
        return new String(result.getValue());
    }

    private Pair<HttpUtil.HttpStatusCode, byte[]> get(String route) throws IOException {
        Pair<HttpUtil.HttpStatusCode, byte[]> result = HttpUtil.get(LauncherApplication.config.UPDATE_URL + "/" + route, List.of());
        if(result.getKey().getCode() < 200 || result.getKey().getCode() >= 300) {
            throw new IOException("The server returned an error code.\nStatus: " + result.getKey() + (result.getValue() != null && result.getValue().length != 0 ? "\nMessage: " + new String(result.getValue()) : ""));
        }
        return result;
    }

}
