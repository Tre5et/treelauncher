package net.treset.minecraftlauncher.creation;

import net.treset.minecraftlauncher.util.exception.ComponentCreationException;

public interface ComponentCreator {
    String getId() throws ComponentCreationException;
}
