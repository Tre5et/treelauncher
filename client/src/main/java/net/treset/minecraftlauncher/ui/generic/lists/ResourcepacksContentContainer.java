package net.treset.minecraftlauncher.ui.generic.lists;

import net.treset.mc_version_loader.resoucepacks.Resourcepack;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.util.FileUtil;
import net.treset.minecraftlauncher.util.LauncherImage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

public class ResourcepacksContentContainer extends FolderContentContainer {
    private final HashMap<ContentElement, File> fileMap = new HashMap<>();

    @Override
    protected ContentElement createElement(File file) {
        try {
            Resourcepack rp = Resourcepack.from(file);
            ContentElement contentElement = new ContentElement(rp.getImage() == null ? null : new LauncherImage(rp.getImage()), rp.getName(), rp.getPackMcmeta().getPack().getDescription(), null);
            fileMap.put(contentElement, file);
            contentElement.setOnDelete(event -> {
                File toDelete = fileMap.get(contentElement);
                if(toDelete != null) {
                    try {
                        if(toDelete.isDirectory())
                            FileUtil.deleteDir(toDelete);
                        else if(toDelete.isFile()) {
                            Files.delete(toDelete.toPath());
                        }
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
