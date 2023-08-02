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

public abstract class SelectorElement<E extends SelectorEntryElement> extends UiElement {
    @FXML protected AnchorPane rootPane;
    @FXML protected VBox vbElements;
    @FXML protected ActionBar abMain;
    @FXML protected CreateSelectable csCreate;
    @FXML protected VBox vbCreate;
    @FXML protected PopupElement icPopupController;

    protected LauncherFiles files;
    protected boolean createSelected = false;
    protected List<Pair<E, AnchorPane>> elements;

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
        vbElements.getChildren().clear();
        if(csCreate != null) {
            csCreate.getStyleClass().remove("selected");
        }
        Platform.runLater(() -> {
            abMain.setDisable(true);
            abMain.clearLabel();
            if(vbCreate != null) {
                vbCreate.setVisible(false);
            }
            for(Pair<E, AnchorPane> element : elements) {
                vbElements.getChildren().add(element.getValue());
            }
        });
    }

    @FXML protected void onSelectCreate() {
        if(!getLock()) {
            if(createSelected) {
                csCreate.getStyleClass().remove("selected");
                setCreatorVisible(false);
                abMain.setDisable(true);
                abMain.clearLabel();
            } else {
                csCreate.getStyleClass().add("selected");
                deselectAll();
                setCreatorVisible(true);
                abMain.setDisable(false);
                abMain.setLabel("components.label.create");
            }
            createSelected = !createSelected;
        }
    }

    protected void setCreatorVisible(boolean visible) {
        if(vbCreate != null) {
            vbCreate.setVisible(visible);
        }
    }

    @FXML
    protected abstract void onCreate();

    @FXML
    protected abstract void onFolder();

    @FXML
    protected void onEdit() {
        icPopupController.setType(PopupElement.PopupType.NONE);
        icPopupController.setContent("selector.component.edit.title", "");
        icPopupController.clearControls();
        icPopupController.setTextInput("selector.component.edit.prompt");
        icPopupController.addButtons(
                new PopupElement.PopupButton(
                        PopupElement.ButtonType.NEGATIVE,
                        "selector.component.edit.cancel",
                        "cancel",
                        id -> icPopupController.setVisible(false)
                ),
                new PopupElement.PopupButton(
                        PopupElement.ButtonType.POSITIVE,
                        "selector.component.edit.confirm",
                        "confirm",
                        id -> {
                            String newName = icPopupController.getTextInputContent();
                            if(!editValid(newName)) {
                                icPopupController.setErrorMessage("selector.component.edit.error");
                                return;
                            }
                            icPopupController.setVisible(false);
                            editCurrent(newName);
                        }
                )
        );
        icPopupController.setVisible(true);
    }

    protected abstract boolean editValid(String newName);

    protected abstract void editCurrent(String newName);

    @FXML
    protected void onDelete() {
        if(getLock()) {
            return;
        }
        String usedBy = getCurrentUsedBy();
        if(usedBy != null) {
            icPopupController.setType(PopupElement.PopupType.ERROR);
            icPopupController.setTitle("selector.component.delete.unable.title");
            icPopupController.setMessage("selector.component.delete.unable.message", usedBy);
            icPopupController.clearControls();
            icPopupController.addButtons(
                    new PopupElement.PopupButton(
                            PopupElement.ButtonType.POSITIVE,
                            "selector.component.delete.unable.close",
                            "close",
                            id -> icPopupController.setVisible(false)
                    )
            );
            icPopupController.setVisible(true);
        } else {
            icPopupController.setType(PopupElement.PopupType.WARNING);
            icPopupController.setContent("selector.component.delete.title", "selector.component.delete.message");
            icPopupController.clearControls();
            icPopupController.addButtons(
                    new PopupElement.PopupButton(
                            PopupElement.ButtonType.NEGATIVE,
                            "selector.component.delete.cancel",
                            "cancel",
                            id -> icPopupController.setVisible(false)
                    ),
                    new PopupElement.PopupButton(
                            PopupElement.ButtonType.POSITIVE,
                            "selector.component.delete.confirm",
                            "confirm",
                            id -> {
                                icPopupController.setVisible(false);
                                deleteCurrent();
                            }
                    )
            );
            icPopupController.setVisible(true);
        }
    }

    protected void deselectAll() {
        for(Pair<E, AnchorPane> element : elements) {
            element.getKey().select(false, true, false);
        }
    }

    protected abstract String getCurrentUsedBy();

    protected abstract void deleteCurrent();

    protected abstract List<Pair<E, AnchorPane>> getElements();

    @Override
    public void beforeShow(Stage stage) {
        reloadComponents();
        for(Pair<E, AnchorPane> element : elements) {
            element.getKey().beforeShow(stage);
        }
    }

    @Override
    public void afterShow(Stage stage) {
        for(Pair<E, AnchorPane> element : elements) {
            element.getKey().afterShow(stage);
        }
    }

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }
}
