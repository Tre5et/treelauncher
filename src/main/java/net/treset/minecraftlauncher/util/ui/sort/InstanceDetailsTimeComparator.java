package net.treset.minecraftlauncher.util.ui.sort;

import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.data.InstanceData;

import java.util.Comparator;

public class InstanceDetailsTimeComparator implements Comparator<InstanceData> {
    @Override
    public int compare(InstanceData o1, InstanceData o2) {
        return (int) (o2.getInstance().getValue().getTotalTime() - o1.getInstance().getValue().getTotalTime());
    }

    @Override
    public String toString() {
        return LauncherApplication.stringLocalizer.get("sort.time");
    }
}
