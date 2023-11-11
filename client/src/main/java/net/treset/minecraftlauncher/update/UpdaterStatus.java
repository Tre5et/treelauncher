package net.treset.minecraftlauncher.update;

import net.treset.mc_version_loader.json.GenericJsonParsable;
import net.treset.minecraftlauncher.ui.generic.popup.PopupElement;

import java.util.List;

public class UpdaterStatus extends GenericJsonParsable {
    public enum Status {
        UPDATING("updater.status.updating", PopupElement.PopupType.ERROR),
        SUCCESS("updater.status.success", PopupElement.PopupType.SUCCESS),
        WARNING("updater.status.warning", PopupElement.PopupType.WARNING),
        FAILURE("updater.status.failure", PopupElement.PopupType.WARNING),
        FATAL("updater.status.fatal", PopupElement.PopupType.ERROR);

        private final String translationKey;
        private final PopupElement.PopupType type;

        Status(String translationKey, PopupElement.PopupType type) {
            this.translationKey = translationKey;
            this.type = type;
        }

        public String getTranslationKey() {
            return translationKey;
        }

        public PopupElement.PopupType getType() {
            return type;
        }
    }

    private Status status;
    private String message;
    private List<String> exceptions;

    public UpdaterStatus(Status status, String message, List<String> exceptions) {
        this.status = status;
        this.message = message;
        this.exceptions = exceptions;
    }

    public static UpdaterStatus fromJson(String json) {
        return fromJson(json, UpdaterStatus.class);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getExceptions() {
        return exceptions;
    }

    public void setExceptions(List<String> exceptions) {
        this.exceptions = exceptions;
    }
}
