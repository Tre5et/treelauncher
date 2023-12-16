package old.util.ui.cellfactory;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import net.treset.mc_version_loader.launcher.LauncherManifest;

public class ManifestListCellFactory implements Callback<ListView<LauncherManifest>, ListCell<LauncherManifest>> {
    @Override
    public ListCell<LauncherManifest> call(ListView<LauncherManifest> param) {
        return new ListCell<>() {
            @Override
            protected void updateItem(LauncherManifest item, boolean empty) {
                super.updateItem(item, empty);
                if(empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        };
    }
}
