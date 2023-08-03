package net.treset.minecraftlauncher.config;

import net.treset.mc_version_loader.json.GenericJsonParsable;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.util.FileUtil;
import net.treset.minecraftlauncher.util.ui.sort.InstanceDetailsLastPlayedComparator;
import net.treset.minecraftlauncher.util.ui.sort.InstanceDetailsNameComparator;
import net.treset.minecraftlauncher.util.ui.sort.InstanceDetailsTimeComparator;

import java.io.File;
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

        public static InstanceDataSortType from(Class<? extends Comparator<InstanceData>> clazz) {
            for(InstanceDataSortType type : values()) {
                if(type.getClazz().equals(clazz)) {
                    return type;
                }
            }
            return NAME;
        }
    }

    private transient File file;

    private InstanceDataSortType instanceSortType;

    public Settings(File file) {
        this.file = file;
        this.instanceSortType = InstanceDataSortType.NAME;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public InstanceDataSortType getInstanceSortType() {
        return instanceSortType;
    }

    public void setInstanceSortType(InstanceDataSortType instanceSortType) {
        this.instanceSortType = instanceSortType;
    }

    public void setInstanceSortType(Class<? extends Comparator<InstanceData>> clazz) {
        this.instanceSortType = InstanceDataSortType.from(clazz);
    }

    @Override
    public void writeToFile(String filePath) throws IOException {
        super.writeToFile(filePath);
    }

    public void save() throws IOException {
        super.writeToFile(file.getAbsolutePath());
    }

    public static Settings load(File file) throws IOException {
        String json;
        json = FileUtil.loadFile(file.getAbsolutePath());
        Settings result = fromJson(json, Settings.class);
        result.setFile(file);
        return result;
    }
}
