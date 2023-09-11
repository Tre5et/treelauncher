package net.treset.minecraftlauncher.ui.generic.lists;

import net.treset.mc_version_loader.resoucepacks.Resourcepack;
import net.treset.minecraftlauncher.util.ImageUtil;

import java.io.File;
import java.io.IOException;

public class ResourcepacksContentContainer extends FolderContentContainer {
    @Override
    protected ContentElement createElement(File file) {
        try {
            Resourcepack rp = Resourcepack.from(file);
            return new ContentElement(rp.getImage() == null ? null : ImageUtil.getImage(rp.getImage()), rp.getName(), rp.getPackMcmeta().getPack().getDescription(), null);
        } catch (IOException e) {
            return null;
        }
    }
}
