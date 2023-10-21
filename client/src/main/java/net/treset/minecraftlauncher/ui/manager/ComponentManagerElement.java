package net.treset.minecraftlauncher.ui.manager;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.ui.generic.IconButton;
import net.treset.minecraftlauncher.ui.generic.popup.PopupElement;
import net.treset.minecraftlauncher.util.file.LauncherFile;
import net.treset.minecraftlauncher.util.string.PatternString;
import net.treset.minecraftlauncher.util.ui.cellfactory.IncludedFilesListCellFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

public class ComponentManagerElement extends VBox {
    private final IconButton btBack = new IconButton();
    private final Label lbIncluded = new Label();
    private final ListView<String> lvIncluded = new ListView<>();
    private final HBox hbButtons = new HBox();
    private final IconButton btAdd = new IconButton();
    private final IconButton btRemove = new IconButton();
    private final VBox vbIncluded = new VBox();

    private LauncherManifest component;

    private EventHandler<ActionEvent> onBack = null;

    public ComponentManagerElement() {
        super();

        btBack.getStyleClass().add("back");
        btBack.setIconSize(36);

        lbIncluded.getStyleClass().add("title");
        lbIncluded.setText(LauncherApplication.stringLocalizer.get("manager.component.includedfiles.title"));

        lvIncluded.setCellFactory(new IncludedFilesListCellFactory());

        btAdd.getStyleClass().add("plus");
        btAdd.setIconSize(24);
        btAdd.setOnAction(this::onAdd);

        btRemove.getStyleClass().addAll("delete", "neutral");
        btRemove.setIconSize(24);
        btRemove.setOnAction(this::onRemove);

        hbButtons.getChildren().addAll(btAdd, btRemove);

        vbIncluded.setPadding(new Insets(10));
        vbIncluded.getChildren().addAll(lbIncluded, lvIncluded, hbButtons);

        getStyleClass().add("element");
        setSpacing(5);
        getChildren().addAll(vbIncluded);
    }

    public void init(LauncherManifest component) {
        this.component = component;

        lvIncluded.getItems().clear();
        lvIncluded.getItems().addAll(component.getIncludedFiles());
    }

    public void save() {
        component.getIncludedFiles().clear();
        component.getIncludedFiles().addAll(lvIncluded.getItems());

        try {
            LauncherFile.of(component.getDirectory(), LauncherApplication.config.MANIFEST_FILE_NAME).write(component);
        } catch (IOException e) {
            LauncherApplication.displayError(e);
        }
    }

    public EventHandler<ActionEvent> getOnBack() {
        return onBack;
    }

    public void setOnBack(EventHandler<ActionEvent> onBack) {
        if(onBack == this.onBack) return;
        this.onBack = onBack;
        btBack.setOnAction(onBack);
        if(this.getChildren().contains(btBack)) return;
        this.getChildren().add(0, btBack);
    }

    private void onAdd(ActionEvent event) {
        PopupElement.PopupComboBox<String> cbType = new PopupElement.PopupComboBox<>(Stream.of("manager.component.type.file", "manager.component.type.folder").map(LauncherApplication.stringLocalizer::get).toList(), 0);
        PopupElement.PopupTextInput tfFile = new PopupElement.PopupTextInput("manager.component.prompt.add");
        LauncherApplication.setPopup(new PopupElement(
                PopupElement.PopupType.NONE,
                "manager.component.add.title",
                "",
                List.of(cbType, tfFile),
                List.of(
                        new PopupElement.PopupButton(
                                PopupElement.ButtonType.NEGATIVE,
                                "manager.component.add.cancel",
                                (e) -> LauncherApplication.setPopup(null)),
                        new PopupElement.PopupButton(
                                PopupElement.ButtonType.POSITIVE,
                                "manager.component.add.confirm",
                                (e) -> {
                                    String file = tfFile.getText();
                                    if(cbType.getSelectedIndex() == 1) {
                                        file += "/";
                                    }
                                    lvIncluded.getItems().add(new PatternString(file).toString());
                                    save();
                                    LauncherApplication.setPopup(null);
                                }
                        )
                )
        ));
    }

    private void onRemove(ActionEvent event) {
        if(lvIncluded.getSelectionModel().getSelectedItem() == null) {
            return;
        }

        lvIncluded.getItems().remove(lvIncluded.getSelectionModel().getSelectedItem());
        save();
    }
}
