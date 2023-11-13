package net.treset.minecraftlauncher.sync;

import net.treset.mc_version_loader.json.GenericJsonParsable;
import net.treset.mc_version_loader.json.SerializationException;
import net.treset.minecraftlauncher.LauncherApplication;

import java.util.Arrays;
import java.util.List;

public class ComponentList extends GenericJsonParsable {
    public static class Entry {
        private final String id;
        private final String name;

        public Entry(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return (name.isBlank() ? LauncherApplication.stringLocalizer.get("sync.component.unknown") : name) + " (" + (id.length() > 8 ? id.substring(0, 7) : id) + "...)";
        }
    }

    public static ComponentList fromJson(String json) throws SerializationException {
        Entry[] entries = GenericJsonParsable.fromJson(json, Entry[].class);
        return new ComponentList(Arrays.asList(entries));
    }

    private final List<Entry> entries;

    public ComponentList(List<Entry> entries) {
        this.entries = entries;
    }

    public List<Entry> getEntries() {
        return entries;
    }
}
