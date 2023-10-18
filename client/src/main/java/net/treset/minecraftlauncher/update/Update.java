package net.treset.minecraftlauncher.update;

import net.treset.mc_version_loader.json.GenericJsonParsable;

import java.util.List;

public class Update extends GenericJsonParsable {
    public enum Mode {
        FILE,
        DELETE,
        REGEX,
        LINE
    }

    public static class Change {
        public static class Element {
            private String pattern;
            private String value;
            private String meta;
            private boolean replace;

            public Element(String pattern, String value, String meta, boolean replace) {
                this.pattern = pattern;
                this.value = value;
                this.meta = meta;
                this.replace = replace;
            }

            public String getPattern() {
                return pattern;
            }

            public void setPattern(String pattern) {
                this.pattern = pattern;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }

            public String getMeta() {
                return meta;
            }

            public void setMeta(String meta) {
                this.meta = meta;
            }

            public boolean isReplace() {
                return replace;
            }

            public void setReplace(boolean replace) {
                this.replace = replace;
            }
        }

        private String path;
        private Mode mode;
        private List<Element> elements;
        private boolean updater;

        public Change(String path, Mode mode, List<Element> elements, boolean updater) {
            this.path = path;
            this.mode = mode;
            this.elements = elements;
            this.updater = updater;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public Mode getMode() {
            return mode;
        }

        public void setMode(Mode mode) {
            this.mode = mode;
        }

        public List<Element> getElements() {
            return elements;
        }

        public void setElements(List<Element> elements) {
            this.elements = elements;
        }

        public boolean isUpdater() {
            return updater;
        }

        public void setUpdater(boolean updater) {
            this.updater = updater;
        }
    }

    private String id;
    private List<Change> changes;
    private String message;
    private boolean latest;

    public Update(String id, List<Change> changes, String message, boolean latest) {
        this.id = id;
        this.changes = changes;
        this.message = message;
        this.latest = latest;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Change> getChanges() {
        return changes;
    }

    public void setChanges(List<Change> changes) {
        this.changes = changes;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isLatest() {
        return latest;
    }

    public void setLatest(boolean latest) {
        this.latest = latest;
    }

    public static Update fromJson(String json) {
        return fromJson(json, Update.class);
    }
}
