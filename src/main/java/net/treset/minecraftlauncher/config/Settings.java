package net.treset.minecraftlauncher.config;

import net.treset.mc_version_loader.json.GenericJsonParsable;
import net.treset.mc_version_loader.launcher.LauncherMod;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.resources.localization.StringLocalizer;
import net.treset.minecraftlauncher.util.FileUtil;
import net.treset.minecraftlauncher.util.ui.sort.*;

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

    public enum LauncherModSortType {
        NAME(new LauncherModNameComparator());

        private final Comparator<LauncherMod> comparator;
        LauncherModSortType(Comparator<LauncherMod> comparator) {
            this.comparator = comparator;
        }

        public Comparator<LauncherMod> getComparator() {
            return comparator;
        }

        @Override
        public String toString() {
            return this.getComparator().toString();
        }
    }

    private transient File file;

    private StringLocalizer.Language language;
    private InstanceDataSortType instanceSortType = InstanceDataSortType.NAME;
    private boolean instanceSortReverse = false;
    private LauncherModSortType modSortType = LauncherModSortType.NAME;
    private boolean modSortReverse = false;
    private boolean modsUpdate = true;
    private boolean modsEnable = false;
    private boolean modsDisable = false;

    public Settings(File file) {
        this.file = file;
        this.language = StringLocalizer.getSystemLanguage();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public StringLocalizer.Language getLanguage() {
        return language;
    }

    public void setLanguage(StringLocalizer.Language language) {
        this.language = language;
    }

    public InstanceDataSortType getInstanceSortType() {
        return instanceSortType == null ? InstanceDataSortType.NAME : instanceSortType;
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

    public LauncherModSortType getModSortType() {
        return modSortType == null ? LauncherModSortType.NAME : modSortType;
    }

    public void setModSortType(LauncherModSortType modSortType) {
        this.modSortType = modSortType;
    }

    public boolean isModSortReverse() {
        return modSortReverse;
    }

    public void setModSortReverse(boolean modSortReverse) {
        this.modSortReverse = modSortReverse;
    }

    public boolean isModsUpdate() {
        return modsUpdate;
    }

    public void setModsUpdate(boolean modsUpdate) {
        this.modsUpdate = modsUpdate;
    }

    public boolean isModsEnable() {
        return modsEnable;
    }

    public void setModsEnable(boolean modsEnable) {
        this.modsEnable = modsEnable;
    }

    public boolean isModsDisable() {
        return modsDisable;
    }

    public void setModsDisable(boolean modsDisable) {
        this.modsDisable = modsDisable;
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
