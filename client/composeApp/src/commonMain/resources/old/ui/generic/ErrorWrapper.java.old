package net.treset.minecraftlauncher.ui.generic;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
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

        setErrorRecursive(this, show);
    }

    private void setErrorRecursive(Pane pane, boolean show) {
        for(Node child : pane.getChildren()) {
            if(child instanceof Pane) {
                setErrorRecursive((Pane) child, show);
            } else {
                if(show) {
                    child.getStyleClass().add("error");
                } else {
                    child.getStyleClass().remove("error");
                }
            }
        }
    }

    public void setErrorMessage(String errorMessage) {
        this.lbError.setText(LauncherApplication.stringLocalizer.get(errorMessage));
    }

    public String getErrorMessage() {
        return lbError.getText();
    }
}
