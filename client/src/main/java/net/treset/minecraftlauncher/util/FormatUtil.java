package net.treset.minecraftlauncher.util;

import net.treset.mc_version_loader.format.FormatUtils;
import net.treset.minecraftlauncher.LauncherApplication;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
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

    public static String hash(Object source) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e); // this doesn't happen
        }

        byte[] encrypted = md.digest((source.toString() + System.nanoTime()).getBytes());
        StringBuilder encryptedString = new StringBuilder(new BigInteger(1, encrypted).toString(16));
        for(int i = encryptedString.length(); i < 32; i++) {
            encryptedString.insert(0, "0");
        }
        return encryptedString.toString();
    }

    public static <T> int indexInList(List<T> list, T item) {
        for(int i = 0; i < list.size(); i++) {
            if(list.get(i).equals(item)) return i;
        }
        return -1;
    }

    public static String absoluteDirPath(String... parts) {
        StringBuilder path = new StringBuilder();
        for(String part : parts) {
            path.append(part);
            if(!part.endsWith(File.separator) && !part.endsWith("/") && !part.endsWith("\\"))
                path.append(File.separator);
        }
        return path.toString();
    }

    public static String absoluteFilePath(String... parts) {
        String dirPath = absoluteDirPath(Arrays.stream(parts).limit(parts.length - 1).toArray(String[]::new));
        return dirPath + parts[parts.length - 1];
    }

    public static String relativeDirPath(String... parts) {
        ArrayList<String> partsList = new ArrayList<>(Arrays.asList(parts));
        partsList.add(0, LauncherApplication.config.BASE_DIR);
        return absoluteDirPath(partsList.toArray(String[]::new));
    }

    public static String relativeFilePath(String... parts) {
        ArrayList<String> partsList = new ArrayList<>(Arrays.asList(parts));
        partsList.add(0, LauncherApplication.config.BASE_DIR);
        return absoluteFilePath(partsList.toArray(String[]::new));
    }

    // \ -> \\; . -> \.; [.*;.+;.?] unchanged; no .* at start / end -> ^ / $
    public static String toRegex(String source) {
        String pattern = source.replaceAll("\\\\", "\\\\\\\\").replaceAll("(?<=^|[^\\\\])\\.(?=[^*+?]|$)", "\\\\.");
        if(!pattern.startsWith(".*")) pattern = "^" + pattern;
        if(!pattern.endsWith(".*")) pattern += "$";
        return pattern;
    }

    public static List<String> toRegex(String... items) {
        return Arrays.stream(items).map(FormatUtil::toRegex).toList();
    }

    public static List<String> toRegex(List<String> items) {
        return items.stream().map(FormatUtil::toRegex).toList();
    }

    public static String fromRegex(String item) {
        return item.replaceAll("\\\\\\\\", "\\\\").replaceAll("\\\\\\.", ".").replaceAll("^\\^", "").replaceAll("\\$$", "");
    }

    public static List<String> fromRegex(String... items) {
        return Arrays.stream(items).map(FormatUtil::fromRegex).toList();
    }

    public static List<String> fromRegex(List<String> items) {
        return items.stream().map(FormatUtil::fromRegex).toList();
    }

    public static String formatSeconds(long seconds) {
        if (seconds < 60) {
            return seconds + LauncherApplication.stringLocalizer.get("suffix.seconds");
        } else if (seconds < 60 * 60) {
            return (seconds / 60) + LauncherApplication.stringLocalizer.get("suffix.minutes");
        } else if (seconds < 60 * 60 * 10) {
            return (seconds / 3600) + LauncherApplication.stringLocalizer.get("suffix.hours") + " " + ((seconds % 3600) / 60) + LauncherApplication.stringLocalizer.get("suffix.minutes");
        } else if (seconds < 60 * 60 * 24) {
            return (seconds / 3600) + LauncherApplication.stringLocalizer.get("suffix.hours");
        } else if (seconds < 60 * 60 * 24 * 10) {
            return (seconds / (3600 * 24)) + LauncherApplication.stringLocalizer.get("suffix.days") + " " + ((seconds % (3600 * 24)) / 3600) + LauncherApplication.stringLocalizer.get("suffix.hours");
        }
        else {
            return (seconds / (3600 * 24)) + "d ";
        }
    }

    public static int distance(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        // i == 0
        int [] costs = new int [b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

    public static String hashFile(File file) throws IOException {
        return hash(FileUtil.readFile(file.getAbsolutePath()));
    }

    public static String urlEncode(String string) {
        return URLEncoder.encode(string, StandardCharsets.UTF_8);
    }

    public static String urlDecode(String string) {
        return java.net.URLDecoder.decode(string, StandardCharsets.UTF_8);
    }
}
