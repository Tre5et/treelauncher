package old.util.ui;

import javafx.event.ActionEvent;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.sync.ComponentList;
import net.treset.minecraftlauncher.sync.FileSynchronizer;
import net.treset.minecraftlauncher.sync.SyncService;
import net.treset.minecraftlauncher.ui.generic.popup.PopupElement;
import net.treset.minecraftlauncher.util.file.LauncherFile;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class FileSyncHelper {
    private final LauncherManifest baseManifest;
    private final List<LauncherManifest> components;

    public FileSyncHelper(LauncherManifest baseManifest, List<LauncherManifest> components) {
        this.baseManifest = baseManifest;
        this.components = components;
    }

    public void showDownloadPopup(Consumer<LauncherManifest> onDone) {
        ComponentList list;
        try {
            list = new SyncService().getAvailable(SyncService.convertType(FileSynchronizer.getChildType(baseManifest.getType())));
        } catch (IOException e) {
            LauncherApplication.displayError(e);
            return;
        }

        List<ComponentList.Entry> entries = list.getEntries().stream()
                .filter(e -> components.stream()
                        .noneMatch(c -> c.getId().equals(e.getId())))
                .toList();

        if(entries.isEmpty()) {
            LauncherApplication.setPopup(
                    new PopupElement(
                            PopupElement.PopupType.NONE,
                            "sync.popup.download.none.title",
                            null,
                            List.of(
                                    new PopupElement.PopupButton(
                                            PopupElement.ButtonType.POSITIVE,
                                            "sync.popup.download.none.close",
                                            (e) -> LauncherApplication.setPopup(null)
                                    )
                            )

                    )
            );
            return;
        }

        PopupElement.PopupComboBox<ComponentList.Entry> cb = new PopupElement.PopupComboBox<>(entries);

        PopupElement.PopupButton button = new PopupElement.PopupButton(
                PopupElement.ButtonType.POSITIVE,
                "sync.popup.download.confirm",
                (ae) -> completeDownload(ae, cb, onDone)
        );
        button.setDisabled(true);

        cb.addOnSelectionChanged((e, o, n) -> button.setDisabled(n == null));

        LauncherApplication.setPopup(new PopupElement(
                PopupElement.PopupType.NONE,
                "sync.popup.download.title",
                "sync.popup.download.message",
                List.of(cb),
                List.of(new PopupElement.PopupButton(
                                PopupElement.ButtonType.NEGATIVE,
                                "sync.popup.download.cancel",
                                event1 -> LauncherApplication.setPopup(null)
                        ),
                        button
                )
        ));
    }

    private void completeDownload(ActionEvent event, PopupElement.PopupComboBox<ComponentList.Entry> comboBox, Consumer<LauncherManifest> onDone) {
        LauncherManifest tempManifest = new LauncherManifest(
                FileSynchronizer.getStringFromType(FileSynchronizer.getChildType(baseManifest.getType()), baseManifest.getTypeConversion()),
                baseManifest.getTypeConversion(),
                comboBox.getSelected().getId(),
                null,
                null,
                null,
                null,
                null

        );
        tempManifest.setDirectory(LauncherFile.of(baseManifest.getDirectory(), baseManifest.getPrefix() + "_" + tempManifest.getId()).getPath());
        onDone.accept(tempManifest);
    }
}
