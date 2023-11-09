package net.treset.minecraftlauncher.news;

import javafx.util.Pair;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.resources.localization.StringLocalizer;
import net.treset.minecraftlauncher.util.HttpService;

import java.io.IOException;

public class NewsService extends HttpService {
    public NewsService() {
        super(LauncherApplication.config.UPDATE_URL);
    }

    public News news() throws IOException {
        Pair<HttpStatusCode, byte[]> result = get("news", LauncherApplication.stringLocalizer.get("launcher.version"), StringLocalizer.getLocale(LauncherApplication.stringLocalizer.getLanguage()).toString());
        String response = new String(result.getValue());
        try {
            return News.fromJson(response);
        } catch(Exception e) {
            throw new IOException("Failed to parse the response from the server.\nError: " + e);
        }
    }
}
