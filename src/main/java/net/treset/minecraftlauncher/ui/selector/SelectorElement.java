package net.treset.minecraftlauncher.ui.selector;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.data.LauncherFiles;
import net.treset.minecraftlauncher.ui.base.UiController;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.ui.generic.ActionBar;
import net.treset.minecraftlauncher.ui.generic.CreateSelectable;
import net.treset.minecraftlauncher.ui.generic.PopupElement;
import net.treset.minecraftlauncher.ui.generic.lists.SelectorEntryElement;
import net.treset.minecraftlauncher.util.exception.FileLoadException;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class SelectorElement<E extends SelectorEntryElement<? extends SelectorEntryElement.ContentProvider>> extends UiElement {
    @FXML protected AnchorPane rootPane;
    @FXML protected VBox vbElements;
    @FXML protected ActionBar abMain;
    @FXML protected CreateSelectable csCreate;
    @FXML protected VBox vbCreate;

    protected LauncherFiles files;
    protected boolean createSelected = false;
    protected List<E> elements;

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
        if(csCreate != null) {
            csCreate.getStyleClass().remove("selected");
        }
        Platform.runLater(() -> {
            abMain.setDisable(true);
            abMain.clearLabel();
            vbElements.getChildren().clear();
            if(vbCreate != null) {
                vbCreate.setVisible(false);
            }
            vbElements.getChildren().addAll(elements);
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
        PopupElement.PopupControl tfName = new PopupElement.PopupControl("selector.component.edit.prompt");

        PopupElement popup = new PopupElement(
                PopupElement.PopupType.NONE,
                "selector.component.edit.title",
                null,
                List.of(tfName),
                null
        );

        popup.setButtons(List.of(
                new PopupElement.PopupButton(
                        PopupElement.ButtonType.NEGATIVE,
                        "selector.component.edit.cancel",
                        event -> LauncherApplication.setPopup(null)
                ),
                new PopupElement.PopupButton(
                        PopupElement.ButtonType.POSITIVE,
                        "selector.component.edit.confirm",
                        event -> {
                            String newName = tfName.getText();
                            if(!editValid(newName)) {
                                popup.setError("selector.component.edit.error");
                                return;
                            }
                            LauncherApplication.setPopup(null);
                            editCurrent(newName);
                        }
                )
        ));

        LauncherApplication.setPopup(popup);
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
            LauncherApplication.setPopup(
                    new PopupElement(
                            PopupElement.PopupType.ERROR,
                            "selector.component.delete.unable.title",
                            LauncherApplication.stringLocalizer.getFormatted("selector.component.delete.unable.message", usedBy),
                            null,
                            List.of(
                                    new PopupElement.PopupButton(
                                            PopupElement.ButtonType.POSITIVE,
                                            "selector.component.delete.unable.close",
                                            event -> LauncherApplication.setPopup(null)
                                    )
                            )
                    )
            );
        } else {
            LauncherApplication.setPopup(
                    new PopupElement(
                            PopupElement.PopupType.WARNING,
                            "selector.component.delete.title",
                            "selector.component.delete.message",
                            null,
                            List.of(
                                    new PopupElement.PopupButton(
                                            PopupElement.ButtonType.NEGATIVE,
                                            "selector.component.delete.cancel",
                                            event -> LauncherApplication.setPopup(null)
                                    ),
                                    new PopupElement.PopupButton(
                                            PopupElement.ButtonType.POSITIVE,
                                            "selector.component.delete.confirm",
                                            event -> {
                                                LauncherApplication.setPopup(null);
                                                deleteCurrent();
                                            }
                                    )
                            )
                    )
            );
        }
    }

    protected void deselectAll() {
        for(E element : elements) {
            element.select(false, true, false);
        }
    }

    protected abstract String getCurrentUsedBy();

    protected abstract void deleteCurrent();

    protected abstract List<E> getElements();

    @Override
    public void beforeShow(Stage stage) {
        reloadComponents();
    }

    @Override
    public void afterShow(Stage stage) {}

    @Override
    public void setRootVisible(boolean visible) {
        rootPane.setVisible(visible);
    }
}
