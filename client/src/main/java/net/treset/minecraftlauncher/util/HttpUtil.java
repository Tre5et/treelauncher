package net.treset.minecraftlauncher.util;

import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class HttpUtil {
    public static Pair<Integer, String> get(String url, List<Pair<String, String>> headerProperties) throws IOException {
        URL query_url = new URL(url);
        HttpURLConnection con = (HttpURLConnection) query_url.openConnection();
        con.setRequestMethod("GET");
        headerProperties.forEach((pair) -> con.setRequestProperty(pair.getKey(), pair.getValue()));

        int status = con.getResponseCode();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        con.disconnect();

        return new Pair<>(status, content.toString());
    }
}
