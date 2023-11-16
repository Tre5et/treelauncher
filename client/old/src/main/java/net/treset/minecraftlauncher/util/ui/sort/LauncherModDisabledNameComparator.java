package net.treset.minecraftlauncher.util.ui.sort;

import net.treset.mc_version_loader.launcher.LauncherMod;
import net.treset.minecraftlauncher.LauncherApplication;

import java.util.Comparator;

public class LauncherModDisabledNameComparator implements Comparator<LauncherMod> {
    @Override
    public int compare(LauncherMod o1, LauncherMod o2) {
        if(o1.isEnabled() == o2.isEnabled()) {
            return o1.getName().compareTo(o2.getName());
        }
        if(o1.isEnabled()) {
            return -1;
        }
        return 1;
    }

    @Override
    public String toString() {
        if(LauncherApplication.stringLocalizer == null) {
            return "sort.disabled.name";
        }
        return LauncherApplication.stringLocalizer.get("sort.disabled.name");
    }
}
