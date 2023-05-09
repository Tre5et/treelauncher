package net.treset.minecraftlauncher.util;

import net.treset.mc_version_loader.format.FormatUtils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
            throw new RuntimeException(e);
        }

        byte[] encrypted = md.digest((source.toString() + System.nanoTime()).getBytes());
        StringBuilder encryptedString = new StringBuilder(new BigInteger(1, encrypted).toString(16));
        for(int i = encryptedString.length(); i < 32; i++) {
            encryptedString.insert(0, "0");
        }
        return encryptedString.toString();
    }
}
