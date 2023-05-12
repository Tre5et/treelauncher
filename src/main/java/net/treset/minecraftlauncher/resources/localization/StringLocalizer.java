package net.treset.minecraftlauncher.resources.localization;

import java.util.Locale;
import java.util.ResourceBundle;

public class StringLocalizer {
    private final Locale locale;

    private final ResourceBundle stringBundle;

    public StringLocalizer() {
        this(Locale.getDefault(Locale.Category.DISPLAY));
    }
    public StringLocalizer(Locale locale) {
        this.locale = locale;
        stringBundle = ResourceBundle.getBundle("lang.strings", locale);
    }

    public String get(String property) {
        return getFormatted(property);
    }

    public String getFormatted(String property, Object... args) {
        return String.format(getTranslated(property), args);
    }

    public String getTranslated(String property) {
        return stringBundle.getString(property);
    }

    public ResourceBundle getStringBundle() {
        return stringBundle;
    }

    public Locale getCurrentLocale() {
        return locale;
    }
}