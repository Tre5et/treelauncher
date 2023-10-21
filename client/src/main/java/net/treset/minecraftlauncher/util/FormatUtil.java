package net.treset.minecraftlauncher.util;

import net.treset.mc_version_loader.launcher.LauncherManifestType;

import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

public class FormatUtil {
    public static class FormatException extends Exception {
        public FormatException(String message) {
            super(message);
        }
        public FormatException(String message, Throwable cause) {
            super(message, cause);
        }
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

    public static String getStringFromType(LauncherManifestType type, Map<String, LauncherManifestType> typeConversion) {
        for (Map.Entry<String, LauncherManifestType> e: typeConversion.entrySet()) {
            if(e.getValue() == type) {
                return e.getKey();
            }
        }
        throw new IllegalStateException("Unable to find string for type " + type);
    }

    public static LauncherManifestType getChildType(LauncherManifestType type) {
        return switch (type) {
            case INSTANCES -> LauncherManifestType.INSTANCE_COMPONENT;
            case VERSIONS -> LauncherManifestType.VERSION_COMPONENT;
            case SAVES -> LauncherManifestType.SAVES_COMPONENT;
            case RESOURCEPACKS -> LauncherManifestType.RESOURCEPACKS_COMPONENT;
            case MODS -> LauncherManifestType.MODS_COMPONENT;
            case OPTIONS -> LauncherManifestType.OPTIONS_COMPONENT;
            default -> throw new IllegalStateException("Unable to find child type for type " + type);
        };
    }

    public static LauncherManifestType getParentType(LauncherManifestType type) {
        return switch (type) {
            case INSTANCE_COMPONENT -> LauncherManifestType.INSTANCES;
            case VERSION_COMPONENT -> LauncherManifestType.VERSIONS;
            case SAVES_COMPONENT -> LauncherManifestType.SAVES;
            case RESOURCEPACKS_COMPONENT -> LauncherManifestType.RESOURCEPACKS;
            case MODS_COMPONENT -> LauncherManifestType.MODS;
            case OPTIONS_COMPONENT -> LauncherManifestType.OPTIONS;
            default -> throw new IllegalStateException("Unable to find parent type for type " + type);
        };
    }

    public static String urlEncode(String string) {
        return URLEncoder.encode(string, StandardCharsets.UTF_8);
    }

    public static String urlDecode(String string) throws FormatException {
        try {
            return URLDecoder.decode(string, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new FormatException("Invalid URL encoding", e);
        }
    }
}
