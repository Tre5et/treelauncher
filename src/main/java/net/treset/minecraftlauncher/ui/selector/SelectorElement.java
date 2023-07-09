package net.treset.minecraftlauncher.ui.selector;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.ui.base.UiController;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.ui.generic.ActionBar;
import net.treset.minecraftlauncher.ui.generic.CreateSelectable;
import net.treset.minecraftlauncher.ui.generic.PopupElement;
import net.treset.minecraftlauncher.ui.generic.SelectorEntryElement;
import net.treset.minecraftlauncher.util.exception.FileLoadException;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class SelectorElement extends UiElement {
    @FXML protected AnchorPane rootPane;
    @FXML protected VBox elementContainer;
    @FXML protected ActionBar actionBar;
    @FXML protected CreateSelectable createSelectable;
    @FXML protected VBox createContainer;
    @FXML protected PopupElement popupController;

    protected LauncherFiles files;
    protected boolean createSelected = false;
    protected List<Pair<SelectorEntryElement, AnchorPane>> elements;

    @Override
    public void init(UiController parent, Function<Boolean, Boolean> lockSetter, Supplier<Boolean> lockGetter) {
        super.init(parent, lockSetter, lockGetter);
        try {
            files = new LauncherFiles();
            files.reloadAll();
        } catch (FileLoadException e) {
            LauncherApplication.displaySevereError(e);
        }
    }

    protected void reloadComponents() {
        try {
            files.reloadAll();
        } catch (FileLoadException e) {
            LauncherApplication.displaySevereError(e);
        }
        elements = getElements();
        elementContainer.getChildren().clear();
        if(createSelectable != null) {
            createSelectable.getStyleClass().remove("selected");
        }
        Platform.runLater(() -> {
            actionBar.setDisable(true);
            actionBar.clearLabel();
            if(createContainer != null) {
                createContainer.setVisible(false);
            }
            for(Pair<SelectorEntryElement, AnchorPane> element : elements) {
                elementContainer.getChildren().add(element.getValue());
            }
        });
    }

    @FXML protected void onCreateSelectableClicked() {
        if(!getLock()) {
            if(createSelected) {
                createSelectable.getStyleClass().remove("selected");
                setCreatorVisible(false);
                actionBar.setDisable(true);
                actionBar.clearLabel();
            } else {
                createSelectable.getStyleClass().add("selected");
                deselectAll();
                setCreatorVisible(true);
                actionBar.setDisable(false);
                actionBar.setLabel("components.label.create");
            }
            createSelected = !createSelected;
        }
    }

    protected void setCreatorVisible(boolean visible) {
        if(createContainer != null) {
            createContainer.setVisible(visible);
        }
    }

    @FXML
    protected abstract void onCreateClicked();

    @FXML
    protected abstract void onFolderClicked();

    @FXML
    protected void onDeleteClicked() {
        if(getLock()) {
            return;
        }
        String usedBy = getCurrentUsedBy();
        if(usedBy != null) {
            popupController.setType(PopupElement.PopupType.ERROR);
            popupController.setTitle("selector.component.delete.unable.title");
            popupController.setMessage("selector.component.delete.unable.message", usedBy);
            popupController.clearButtons();
            popupController.addButtons(
                    new PopupElement.PopupButton(
                            PopupElement.ButtonType.POSITIVE,
                            "selector.component.delete.unable.close",
                            "close",
                            id -> popupController.setVisible(false)
                    )
            );
            popupController.setVisible(true);
        } else {
            popupController.setType(PopupElement.PopupType.WARNING);
            popupController.setContent("selector.component.delete.title", "selector.component.delete.message");
            popupController.clearButtons();
            popupController.addButtons(
                    new PopupElement.PopupButton(
                            PopupElement.ButtonType.NEGATIVE,
                            "selector.component.delete.cancel",
                            "cancel",
                            id -> popupController.setVisible(false)
                    ),
                    new PopupElement.PopupButton(
                            PopupElement.ButtonType.POSITIVE,
                            "selector.component.delete.confirm",
                            "confirm",
                            id -> {
                                popupController.setVisible(false);
                                deleteCurrent();
                            }
                    )
            );
            popupController.setVisible(true);
        }
    }

    protected void deselectAll() {
        for(Pair<SelectorEntryElement, AnchorPane> element : elements) {
            element.getKey().select(false, true, false);
        }
    }

    protected abstract String getCurrentUsedBy();

    protected abstract void deleteCurrent();

    protected abstract List<Pair<SelectorEntryElement, AnchorPane>> getElements();

    @Override
    public void beforeShow(Stage stage) {
        reloadComponents();
        for(Pair<SelectorEntryElement, AnchorPane> element : elements) {
            element.getKey().beforeShow(stage);
        }
    }

    @Override
    public void afterShow(Stage stage) {
        for(Pair<SelectorEntryElement, AnchorPane> element : elements) {
            element.getKey().afterShow(stage);
        }
    }

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }
}
