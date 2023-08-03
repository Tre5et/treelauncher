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
        NAME(new InstanceDetailsNameComparator()),
        TIME(new InstanceDetailsTimeComparator()),
        LAST_PLAYED(new InstanceDetailsLastPlayedComparator());

        private final Comparator<InstanceData> comparator;
        InstanceDataSortType(Comparator<InstanceData> comparator) {
            this.comparator = comparator;
        }

        public Comparator<InstanceData> getComparator() {
            return comparator;
        }

        @Override
        public String toString() {
            return this.getComparator().toString();
        }
    }

    private transient File file;

    private InstanceDataSortType instanceSortType;
    private boolean instanceSortReverse;

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

    public boolean isInstanceSortReverse() {
        return instanceSortReverse;
    }

    public void setInstanceSortReverse(boolean instanceSortReverse) {
        this.instanceSortReverse = instanceSortReverse;
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
