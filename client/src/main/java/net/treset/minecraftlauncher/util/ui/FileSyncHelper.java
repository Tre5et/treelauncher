package net.treset.minecraftlauncher.util.ui;

import javafx.application.Platform;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.ui.generic.PopupElement;
import net.treset.minecraftlauncher.util.SyncUtil;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class FileSyncHelper<T> {
    public T componentManifest;

    private SyncAction<T> action;

    public FileSyncHelper(T componentManifest, SyncAction<T> action) {
        this.componentManifest = componentManifest;
        this.action = action;
    }

    public void run(Runnable onDone) {
        handlePopup(onDone);
    }

    public T getComponentManifest() {
        return componentManifest;
    }

    public void setComponentManifest(T componentManifest) {
        this.componentManifest = componentManifest;
    }

    public SyncAction<T> getAction() {
        return action;
    }

    public void setAction(SyncAction<T> action) {
        this.action = action;
    }

    private void handlePopup(Runnable onDone) {
        PopupElement popup = new PopupElement(
                PopupElement.PopupType.NONE,
                "sync.popup.syncing",
                null
        );
        LauncherApplication.setPopup(popup);
        new Thread(() -> {
            try {
                action.run(componentManifest, (status) -> {
                    StringBuilder message = new StringBuilder(LauncherApplication.stringLocalizer.get(status.getStep().getTranslationKey()));
                    if(status.getStatus() != null) {
                        message.append("\n").append(status.getStatus().getCurrentFile()).append("\n(").append(status.getStatus().getCurrentAmount()).append("/").append(status.getStatus().getTotalAmount()).append(")");
                    }
                    Platform.runLater(()-> popup.setMessage(message.toString()));
                });
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

    public interface SyncAction<T> {
        void run(T manifest, Consumer<SyncUtil.SyncStatus> statusCallback) throws IOException;
    }
}
