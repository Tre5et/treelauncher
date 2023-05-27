package net.treset.minecraftlauncher.config;

import net.treset.minecraftlauncher.resources.localization.StringLocalizer;
import net.treset.minecraftlauncher.util.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class GlobalConfigLoader {
    private static final Logger LOGGER = LogManager.getLogger(GlobalConfigLoader.class);

    public static Config loadConfig() throws IllegalStateException {
        String contents = FileUtil.loadFile("launcher.conf");
        if(contents == null) {
            throw new IllegalStateException("Unable to load launcher.conf");
        }
        String[] lines = contents.split("\n");
        String path = null;
        boolean debug = false;
        StringLocalizer.Language language = null;
        for(String line : lines) {
            if(line.startsWith("path=")) {
                path = line.substring(5).replace("\r", "").replace("\n", "");
            } else if(line.startsWith("debug=")) {
                debug = Boolean.parseBoolean(line.substring(6));
            } else if(line.startsWith("language=")) {
                String input = line.substring(9).replace("\r", "").replace("\n", "");
                language = StringLocalizer.Language.fromId(input);
            }
        }
        if(path == null || path.isBlank()) {
            throw new IllegalStateException("No path specified in launcher.conf");
        }
        LOGGER.info("Loaded config: path={}, debug={}", path, debug);
        return new Config(path, debug, language);
    }

    public static boolean updateLanguage(StringLocalizer.Language language) {
        String contents = FileUtil.loadFile("launcher.conf");
        if(contents == null) {
            LOGGER.error("Unable to load launcher.conf");
            return false;
        }
        String[] lines = contents.split("\n");
        StringBuilder newContents = new StringBuilder();
        boolean found = false;
        for(String line : lines) {
            if(line.startsWith("language=")) {
                newContents.append("language=").append(language.name()).append("\n");
                found = true;
            } else {
                newContents.append(line).append("\n");
            }
        }
        if(!found) {
            newContents.append("language=").append(language.name()).append("\n");
        }

        return FileUtil.writeFile("launcher.conf", newContents.toString());
    }
}
