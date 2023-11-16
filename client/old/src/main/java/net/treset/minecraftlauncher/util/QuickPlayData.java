package net.treset.minecraftlauncher.util;

public class QuickPlayData {
    public enum Type {
        WORLD,
        SERVER,
        REALM
    }

    private final Type type;
    private final String name;

    public QuickPlayData(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
