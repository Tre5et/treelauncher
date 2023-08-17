package net.treset.minecraftlauncher.ui.generic.lists;

import net.treset.mc_version_loader.saves.Save;
import net.treset.minecraftlauncher.util.ImageUtil;

import java.io.File;
import java.io.IOException;

public class SavesContentContainer extends FolderContentContainer {
    @Override
    protected ContentElement createElement(File file) {
        try {
            Save save = Save.from(file);
            return new ContentElement(save.getImage() == null ? null : ImageUtil.getImage(save.getImage()), save.getName(), save.getFileName(), this::onSelect);
        } catch (IOException e) {
            return null;
        }
    }
}
