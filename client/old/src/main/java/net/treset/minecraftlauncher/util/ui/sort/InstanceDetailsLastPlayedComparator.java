package net.treset.minecraftlauncher.util.ui.sort;

import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.data.InstanceData;

import java.util.Comparator;
import java.util.Objects;

public class InstanceDetailsLastPlayedComparator implements Comparator<InstanceData> {

    @Override
    public int compare(InstanceData o1, InstanceData o2) {
        if(Objects.equals(o1.getInstance().getValue().getLastPlayed(), o2.getInstance().getValue().getLastPlayed())) {
            return 0;
        }
        if(o1.getInstance().getValue().getLastPlayed() == null) {
            return 1;
        }
        if(o2.getInstance().getValue().getLastPlayed() == null) {
            return -1;
        }
        return o2.getInstance().getValue().getLastPlayed().compareTo(o1.getInstance().getValue().getLastPlayed());
    }

    @Override
    public String toString() {
        if(LauncherApplication.stringLocalizer == null) {
            return "sort.lastplayed";
        }
        return LauncherApplication.stringLocalizer.get("sort.lastplayed");
    }
}
