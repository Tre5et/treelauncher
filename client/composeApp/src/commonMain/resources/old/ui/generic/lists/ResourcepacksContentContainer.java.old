package net.treset.minecraftlauncher.ui.generic.lists;

import net.treset.mc_version_loader.resoucepacks.Resourcepack;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.util.LauncherImage;
import net.treset.minecraftlauncher.util.file.LauncherFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class ResourcepacksContentContainer extends FolderContentContainer {
    private final HashMap<ContentElement, LauncherFile> fileMap = new HashMap<>();

    @Override
    protected ContentElement createElement(File file) {
        try {
            Resourcepack rp = Resourcepack.from(file);
            ContentElement contentElement = new ContentElement(rp.getImage() == null ? null : new LauncherImage(rp.getImage()), rp.getName(), rp.getPackMcmeta().getPack().getDescription(), null);
            fileMap.put(contentElement, LauncherFile.of(file));
            contentElement.setOnDelete(event -> {
                LauncherFile toDelete = fileMap.get(contentElement);
                if(toDelete != null) {
                    try {
                        toDelete.remove();
                    } catch (IOException e) {
                        LauncherApplication.displayError(e);
                        return;
                    }
                    new Thread(this::updateElements).start();
                }
            });
            return contentElement;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void updateElements() {
        fileMap.clear();
        super.updateElements();
    }
}
