package net.treset.minecraftlauncher.util.ui.sort;

import net.treset.mc_version_loader.launcher.LauncherMod;
import net.treset.minecraftlauncher.LauncherApplication;

import java.util.Comparator;

public class LauncherModNameComparator implements Comparator<LauncherMod> {
    @Override
    public int compare(LauncherMod o1, LauncherMod o2) {
        return o1.getName().compareTo(o2.getName());
    }

    @Override
    public String toString() {
        if(LauncherApplication.stringLocalizer == null) {
            return "sort.name";
        }
        return LauncherApplication.stringLocalizer.get("sort.name");
    }
}
