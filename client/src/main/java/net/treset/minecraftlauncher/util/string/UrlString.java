package net.treset.minecraftlauncher.util.string;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UrlString extends FormatString {
    private final String original;
    private final boolean encode;

    public UrlString(String original, boolean encode) {
        this.original = original;
        this.encode = encode;
    }

    @Override
    public String get() throws FormatException {
        if(encode) {
            return URLEncoder.encode(original, StandardCharsets.UTF_8);
        } else {
            try {
                return URLDecoder.decode(original, StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                throw new FormatException("Failed to decode URL string: " + original, e);
            }
        }
    }

    public String getOriginal() {
        return original;
    }

    public static UrlString encoded(String original) {
        return new UrlString(original, true);
    }

    public static UrlString decoded(String original) {
        return new UrlString(original, false);
    }
}
