package net.treset.minecraftlauncher.util;

import net.treset.mc_version_loader.format.FormatUtils;

import java.util.List;

public class FormatUtil {
    public static boolean matchesAny(String test, List<String> patterns) {
        if(patterns == null) return false;
        for(String pattern : patterns) {
            if(FormatUtils.matches(test, pattern)) return true;
        }
        return false;
    }

    public static String toRegexPattern(String source) {
        return source.replaceAll("\\.", "\\\\.");
    }
}
