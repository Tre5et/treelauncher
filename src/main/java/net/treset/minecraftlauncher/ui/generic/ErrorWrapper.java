package net.treset.minecraftlauncher.ui.generic;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import net.treset.minecraftlauncher.LauncherApplication;

public class ErrorWrapper extends VBox {
    private final Label lbError = new Label();

    public ErrorWrapper() {
        super();
        lbError.getStyleClass().add("error");
        lbError.setWrapText(true);
        lbError.setText(LauncherApplication.stringLocalizer.get("error.unknown"));
    }

    public void showError(boolean show) {
        if(show) {
            if(getChildren().contains(lbError)) return;
            getChildren().add(lbError);
        } else {
            getChildren().remove(lbError);
        }
    }

    public void setErrorMessage(String errorMessage) {
        this.lbError.setText(LauncherApplication.stringLocalizer.get(errorMessage));
    }

    public String getErrorMessage() {
        return lbError.getText();
    }

}
