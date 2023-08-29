package net.treset.minecraftlauncher.ui.create;

import net.treset.minecraftlauncher.creation.ResourcepackCreator;

public class ResourcepacksCreatorElement extends CreatorElement {
    @Override
    public ResourcepackCreator getCreator() {
        if(!isCreateReady()) {
            throw new IllegalStateException("Creator not ready");
        }

        if(rbCreate.isSelected()) {
            return new ResourcepackCreator(tfCreate.getText(), typeConversion, topManifest);
        } else if(rbInherit.isSelected()) {
            return new ResourcepackCreator(tfInherit.getText(), cbInherit.getValue(), topManifest);
        } else if(rbUse.isSelected()) {
            return new ResourcepackCreator(cbUse.getValue());
        } else {
            throw new IllegalStateException("No radio button selected");
        }
    }
}
