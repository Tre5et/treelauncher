package net.treset.minecraftlauncher.ui.create;

import net.treset.minecraftlauncher.creation.OptionsCreator;

public class OptionsCreatorElement extends CreatorElement {
    @Override
    public OptionsCreator getCreator() {
        if(!isCreateReady()) {
            throw new IllegalStateException("Creator not ready");
        }

        if(rbCreate.isSelected()) {
            return new OptionsCreator(tfCreate.getText(), typeConversion, topManifest);
        } else if(rbInherit.isSelected()) {
            return new OptionsCreator(tfInherit.getText(), cbInherit.getValue(), topManifest);
        } else if(rbUse.isSelected()) {
            return new OptionsCreator(cbUse.getValue());
        } else {
            throw new IllegalStateException("No radio button selected");
        }
    }
}
