package net.treset.minecraftlauncher.sync;

import net.treset.mc_version_loader.json.GenericJsonParsable;

import java.util.List;

public class ComponentData extends GenericJsonParsable {
    public static class HashEntry {
        private final String path;
        private List<HashEntry> children;
        private String hash;

        public HashEntry(String path, String hash) {
            this.path = path;
            this.hash = hash;
        }

        public HashEntry(String path, List<HashEntry> children) {
            this.path = path;
            this.children = children;
        }

        public String getPath() {
            return path;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public List<HashEntry> getChildren() {
            return children;
        }

        public void setChildren(List<HashEntry> children) {
            this.children = children;
        }
    }

    private int version;
    private int fileAmount;
    private List<HashEntry> hashTree;
    public ComponentData(int version, int fileAmount, List<HashEntry> hashTree) {
        this.version = version;
        this.fileAmount = fileAmount;
        this.hashTree = hashTree;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getFileAmount() {
        return fileAmount;
    }

    public void setFileAmount(int fileAmount) {
        this.fileAmount = fileAmount;
    }

    public List<HashEntry> getHashTree() {
        return hashTree;
    }

    public void setHashTree(List<HashEntry> hashTree) {
        this.hashTree = hashTree;
    }

    public static ComponentData fromJson(String json) {
        return fromJson(json, ComponentData.class);
    }
}
