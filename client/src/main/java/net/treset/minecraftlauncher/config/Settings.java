package net.treset.minecraftlauncher.config;

import net.treset.mc_version_loader.json.GenericJsonParsable;
import net.treset.mc_version_loader.launcher.LauncherMod;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.resources.localization.StringLocalizer;
import net.treset.minecraftlauncher.util.file.LauncherFile;
import net.treset.minecraftlauncher.util.ui.sort.*;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

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
        NAME(new LauncherModNameComparator()),
        DISABLED_NAME(new LauncherModDisabledNameComparator());

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

    private transient LauncherFile file;

    private StringLocalizer.Language language;
    private String syncUrl;
    private String syncPort;
    private String syncKey;
    private InstanceDataSortType instanceSortType = InstanceDataSortType.NAME;
    private boolean instanceSortReverse = false;
    private LauncherModSortType modSortType = LauncherModSortType.NAME;
    private boolean modSortReverse = false;
    private boolean modsUpdate = true;
    private boolean modsEnable = false;
    private boolean modsDisable = false;
    private List<String> acknowledgedNews = List.of();

    public Settings(LauncherFile file) {
        this.file = file;
        this.language = StringLocalizer.getSystemLanguage();
    }

    public LauncherFile getFile() {
        return file;
    }

    public void setFile(LauncherFile file) {
        this.file = file;
    }

    public StringLocalizer.Language getLanguage() {
        return language;
    }

    public void setLanguage(StringLocalizer.Language language) {
        this.language = language;
    }

    public boolean hasSyncData() {
        return syncUrl != null && syncPort != null && syncKey != null;
    }

    public String getSyncUrl() {
        return syncUrl;
    }

    public void setSyncUrl(String syncUrl) {
        this.syncUrl = syncUrl;
    }

    public String getSyncPort() {
        return syncPort;
    }

    public void setSyncPort(String syncPort) {
        this.syncPort = syncPort;
    }

    public String getSyncKey() {
        return syncKey;
    }

    public void setSyncKey(String syncKey) {
        this.syncKey = syncKey;
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

    public List<String> getAcknowledgedNews() {
        return acknowledgedNews;
    }

    public void setAcknowledgedNews(List<String> acknowledgedNews) {
        this.acknowledgedNews = acknowledgedNews;
    }

    public void save() throws IOException {
        file.write(this);
    }

    public static Settings fromJson(String json) {
        return fromJson(json, Settings.class);
    }

    public static Settings load(LauncherFile file) throws IOException {
        Settings result = fromJson(file.readString());
        result.setFile(file);
        return result;
    }
}
