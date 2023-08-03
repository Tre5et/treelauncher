package net.treset.minecraftlauncher.config;

import net.treset.mc_version_loader.json.GenericJsonParsable;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.util.ui.sort.InstanceDetailsLastPlayedComparator;
import net.treset.minecraftlauncher.util.ui.sort.InstanceDetailsNameComparator;
import net.treset.minecraftlauncher.util.ui.sort.InstanceDetailsTimeComparator;

import java.io.IOException;
import java.util.Comparator;

public class Settings extends GenericJsonParsable {
    public enum InstanceDataSortType {
        NAME(InstanceDetailsNameComparator.class),
        TIME(InstanceDetailsTimeComparator.class),
        LAST_PLAYED(InstanceDetailsLastPlayedComparator.class);

        private final Class<? extends Comparator<InstanceData>> clazz;
        InstanceDataSortType(Class<? extends Comparator<InstanceData>> clazz) {
            this.clazz = clazz;
        }

        public Class<? extends Comparator<InstanceData>> getClazz() {
            return clazz;
        }

        public String toString() {
            return this.name();
        }

        public static InstanceDataSortType from(Class<? extends Comparator<InstanceData>> clazz) {
            for(InstanceDataSortType type : values()) {
                if(type.getClazz().equals(clazz)) {
                    return type;
                }
            }
            return NAME;
        }
    }

    private InstanceDataSortType instanceSortType;

    public Settings() {
        this.instanceSortType = InstanceDataSortType.NAME;
    }

    public InstanceDataSortType getInstanceSortType() {
        return instanceSortType;
    }

    public void setInstanceSortType(InstanceDataSortType instanceSortType) {
        this.instanceSortType = instanceSortType;
    }

    @Override
    public void writeToFile(String filePath) throws IOException {
        super.writeToFile(filePath);
    }

    public static Settings fromJson(String json) {
        return fromJson(json, Settings.class);
    }
}
