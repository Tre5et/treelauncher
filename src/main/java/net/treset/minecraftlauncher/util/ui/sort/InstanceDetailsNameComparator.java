package net.treset.minecraftlauncher.util.ui.sort;

import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.data.InstanceData;

import java.util.Comparator;

public class InstanceDetailsNameComparator implements Comparator<InstanceData> {
    @Override
    public int compare(InstanceData e1, InstanceData e2) {
        return e1.getInstance().getKey().getName().compareTo(e2.getInstance().getKey().getName());
    }

    @Override
    public String toString() {
        return LauncherApplication.stringLocalizer.get("sort.name");
    }
}
