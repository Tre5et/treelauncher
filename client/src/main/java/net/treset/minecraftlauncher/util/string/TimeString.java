package net.treset.minecraftlauncher.util.string;

import net.treset.minecraftlauncher.LauncherApplication;

public class TimeString extends FormatString {
    private final String output;

    public TimeString(long seconds) {
        if (seconds < 60) {
            this.output = seconds + LauncherApplication.stringLocalizer.get("suffix.seconds");
        } else if (seconds < 60 * 60) {
            this.output = (seconds / 60) + LauncherApplication.stringLocalizer.get("suffix.minutes");
        } else if (seconds < 60 * 60 * 10) {
            this.output = (seconds / 3600) + LauncherApplication.stringLocalizer.get("suffix.hours") + " " + ((seconds % 3600) / 60) + LauncherApplication.stringLocalizer.get("suffix.minutes");
        } else if (seconds < 60 * 60 * 24) {
            this.output = (seconds / 3600) + LauncherApplication.stringLocalizer.get("suffix.hours");
        } else if (seconds < 60 * 60 * 24 * 10) {
            this.output = (seconds / (3600 * 24)) + LauncherApplication.stringLocalizer.get("suffix.days") + " " + ((seconds % (3600 * 24)) / 3600) + LauncherApplication.stringLocalizer.get("suffix.hours");
        } else {
            this.output =  (seconds / (3600 * 24)) + LauncherApplication.stringLocalizer.get("suffix.days");
        }
    }

    public String get() {
        return this.output;
    }
}
