package net.treset.minecraftlauncher.util.string;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UrlString extends FormatString {
    public enum UrlOperation {
        ENCODE,
        DECODE,
        NONE
    }

    private final String original;
    private final UrlOperation operation;

    public UrlString(String original, UrlOperation operation) {
        this.original = original;
        this.operation = operation;
    }

    @Override
    public String get() throws FormatException {
        if(operation == UrlOperation.ENCODE) {
            return URLEncoder.encode(original, StandardCharsets.UTF_8);
        } else if(operation == UrlOperation.DECODE) {
            try {
                return URLDecoder.decode(original, StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                throw new FormatException("Failed to decode URL string: " + original, e);
            }
        } else {
            return original;
        }
    }

    public void openInBrowser() throws IOException, URISyntaxException {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            desktop.browse(new URL(getOriginal()).toURI());
        }
    }

    public String getOriginal() {
        return original;
    }

    public static UrlString of(String original) {
        return new UrlString(original, UrlOperation.NONE);
    }

    public static UrlString encoded(String original) {
        return new UrlString(original, UrlOperation.ENCODE);
    }

    public static UrlString decoded(String original) {
        return new UrlString(original, UrlOperation.DECODE);
    }
}
