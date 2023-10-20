package net.treset.minecraftlauncher.util.string;

import java.util.Arrays;
import java.util.List;

public class PatternString extends FormatString {
    private String pattern;

    public PatternString(String original) {
        this(original, false);
    }

    public PatternString(String original, boolean keep) {
        if(keep) {
            this.pattern = original;
        } else {
            this.pattern = original.replaceAll("\\\\", "\\\\\\\\").replaceAll("(?<=^|[^\\\\])\\.(?=[^*+?]|$)", "\\\\.");
            if(!pattern.startsWith(".*")) pattern = "^" + pattern;
            if(!pattern.endsWith(".*")) pattern += "$";
        }
    }

    public boolean matches(String test) {
        if(test == null) return false;
        return test.matches(get());
    }

    // \ -> \\; . -> \.; [.*;.+;.?] unchanged; no .* at start / end -> ^ / $
    public String get() {
        return pattern;
    }

    public static List<PatternString> toPattern(String... items) {
        return Arrays.stream(items).map(PatternString::new).toList();
    }

    public static String decode(String pattern) {
        return pattern.replaceAll("\\\\\\\\", "\\\\")
                .replaceAll("\\\\\\.", ".")
                .replaceAll("^\\^", "")
                .replaceAll("\\$$", "");
    }

    public static List<String> decode(String... patterns) {
        return Arrays.stream(patterns).map(PatternString::decode).toList();
    }

    public static boolean matchesAny(String test, List<PatternString> patterns) {
        if(patterns == null) return false;
        return patterns.stream().anyMatch(p -> p.matches(test));
    }
}
