package net.treset.minecraftlauncher.util.ui;

import javafx.application.Platform;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.sync.FileSynchronizer;
import net.treset.minecraftlauncher.ui.generic.popup.PopupElement;

import java.io.IOException;
import java.util.List;

public class FileSyncExecutor {
    private final FileSynchronizer synchronizer;
    public FileSyncExecutor(FileSynchronizer synchronizer) {
        this.synchronizer = synchronizer;
    }

    public void upload(Runnable onDone) {
        handlePopup(onDone, true);
    }

    public void download(Runnable onDone) {
        handlePopup(onDone, false);
    }

    private void handlePopup(Runnable onDone, boolean upload) {
        PopupElement popup = new PopupElement(
                PopupElement.PopupType.NONE,
                "sync.popup.syncing",
                null
        );
        synchronizer.setCallback((status) -> {
            StringBuilder message = new StringBuilder(LauncherApplication.stringLocalizer.get(status.getStep().getTranslationKey()));
            if(status.getStatus() != null) {
                message.append("\n").append(status.getStatus().getCurrentFile()).append("\n(").append(status.getStatus().getCurrentAmount()).append("/").append(status.getStatus().getTotalAmount()).append(")");
            }
            Platform.runLater(()-> popup.setMessage(message.toString()));
        });
        LauncherApplication.setPopup(popup);
        new Thread(() -> {
            try {
                if(upload) {
                    synchronizer.upload();
                } else {
                    synchronizer.download();
                }
            } catch (IOException e) {
                LauncherApplication.displayError(e);
                LauncherApplication.setPopup(null);
                return;
            }

            LauncherApplication.setPopup(new PopupElement(
                    PopupElement.PopupType.SUCCESS,
                    "sync.popup.complete",
                    null,
                    List.of(
                            new PopupElement.PopupButton(
                                    PopupElement.ButtonType.POSITIVE,
                                    "sync.popup.complete.close",
                                    (e) -> LauncherApplication.setPopup(null)
                            )
                    )
            ));
            onDone.run();
        }).start();
    }
}
