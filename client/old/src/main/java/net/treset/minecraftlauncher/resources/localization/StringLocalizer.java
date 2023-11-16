package net.treset.minecraftlauncher.resources.localization;

import net.treset.minecraftlauncher.LauncherApplication;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class StringLocalizer {
    private Language language;

    private final ResourceBundle stringBundle;

    public StringLocalizer(Language language) {
        this.language = language != null ? language : getSystemLanguage();
        stringBundle = ResourceBundle.getBundle("lang.strings", this.language.locale);
    }

    public String get(String property) {
        return getFormatted(property);
    }

    public String getFormatted(String property, Object... args) {
        return String.format(getTranslated(property), args);
    }

    public String getTranslated(String property) {
        if(stringBundle.containsKey(property)) {
            return stringBundle.getString(property);
        }
        return property;
    }

    public ResourceBundle getStringBundle() {
        return stringBundle;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
        LauncherApplication.settings.setLanguage(language);
    }

    public static List<Language> getAvailableLanguages() {
        return List.of(Language.values());
    }

    public enum Language {
        ENGLISH("language.english", Locale.ENGLISH),
        GERMAN("language.german", Locale.GERMAN);

        private final String name;
        private final Locale locale;
        Language(String name, Locale locale) {
            this.name = name;
            this.locale = locale;
        }

        public String getName() {
            return LauncherApplication.stringLocalizer.get(name);
        }

        public Locale getLocale() {
            return locale;
        }

        @Override
        public String toString() {
            if(LauncherApplication.stringLocalizer == null) {
                return name;
            }
            return LauncherApplication.stringLocalizer.get(name) + (this.equals(getSystemLanguage()) ? LauncherApplication.stringLocalizer.get("language.default") : "");
        }
    }

    public static Language getSystemLanguage() {
        switch(Locale.getDefault(Locale.Category.DISPLAY).getLanguage()) {
            case "de" -> {
                return Language.GERMAN;
            }
            default -> {
                return Language.ENGLISH;
            }
        }
    }
}