package net.treset.minecraftlauncher.news;

import javafx.util.Pair;
import net.treset.mc_version_loader.json.SerializationException;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.util.HttpService;

import java.io.IOException;

public class NewsService extends HttpService {
    public NewsService() {
        super(LauncherApplication.config.UPDATE_URL);
    }

    public News news() throws IOException {
        Pair<HttpStatusCode, byte[]> result = get("news", LauncherApplication.stringLocalizer.get("launcher.version"), LauncherApplication.stringLocalizer.getLanguage().getLocale());
        String response = new String(result.getValue());
        try {
            return News.fromJson(response);
        } catch(SerializationException e) {
            throw new IOException("Failed to parse the response from the server.\nError: " + e);
        }
    }
}
