package net.treset.minecraftlauncher.util.ui.cellfactory;

import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import net.treset.minecraftlauncher.util.string.PatternString;
import org.kordamp.ikonli.javafx.FontIcon;

public class IncludedFilesListCellFactory implements Callback<ListView<String>, ListCell<String>> {
    @Override
    public ListCell<String> call(ListView<String> param) {
        return new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if(empty || item == null) {
                    setHeight(0);
                    setText(null);
                    setGraphic(null);
                } else {
                    setPadding(new Insets(3, 0, 3, 0));
                    item = PatternString.decode(item);
                    FontIcon icon = new FontIcon();
                    icon.setIconSize(24);
                    icon.getStyleClass().add("icon");
                    if(item.endsWith("/") || item.endsWith("\\")) {
                        setText(item.substring(0, item.length() - 1));
                        icon.getStyleClass().add("folder");
                        setGraphic(icon);
                    } else {
                        setText(item);
                        icon.getStyleClass().add("file");
                        setGraphic(icon);
                    }
                }
            }
        };
    }
}
