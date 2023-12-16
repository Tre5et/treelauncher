package net.treset.minecraftlauncher.ui.create;

import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.minecraftlauncher.creation.SavesCreator;

import java.util.List;
import java.util.Map;

public class SavesCreatorElement extends CreatorElement {
    private LauncherManifest gameManifest;

    public void init(List<LauncherManifest> components, Map<String, LauncherManifestType> typeConversion, LauncherManifest topManifest, LauncherManifest gameManifest) {
        super.init(components, typeConversion, topManifest);
        this.gameManifest = gameManifest;
    }

    @Override
    public SavesCreator getCreator() {
        if(!isCreateReady()) {
            throw new IllegalStateException("Creator not ready");
        }

        if(rbCreate.isSelected()) {
            return new SavesCreator(tfCreate.getText(), typeConversion, topManifest, gameManifest);
        } else if(rbInherit.isSelected()) {
            return new SavesCreator(tfInherit.getText(), cbInherit.getValue(), topManifest, gameManifest);
        } else if(rbUse.isSelected()) {
            return new SavesCreator(cbUse.getValue());
        } else {
            throw new IllegalStateException("No radio button selected");
        }
    }
}
