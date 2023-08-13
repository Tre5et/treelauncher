package net.treset.minecraftlauncher.ui.generic;

import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;

public class ButtonBox<T> extends HBox {

    private final ComboBox<T> cbSort = new ComboBox<>();
    private final IconButton btSort = new IconButton();

    private String iconClass;
    private boolean reverse = false;

    public ButtonBox() {
        super();
        cbSort.getStyleClass().add("button-box");
        btSort.getStyleClass().add("neutral");
        btSort.setIconSize(22);
        getChildren().addAll(cbSort, btSort);
        setSpacing(-50);
        setAlignment(Pos.CENTER_RIGHT);
    }

    public String getIconClass() {
        return iconClass;
    }

    public void setIconClass(String iconClass) {
        btSort.getStyleClass().remove(this.iconClass);
        this.iconClass = iconClass;
        btSort.getStyleClass().add(iconClass);
    }

    public final void setOnAction(EventHandler<ActionEvent> value) {
        btSort.onActionProperty().set(value);
    }
    public final EventHandler<ActionEvent> getOnAction() {
        return btSort.onActionProperty().get();
    }

    public void setItems(T... items) {
        cbSort.getItems().clear();
        cbSort.getItems().addAll(items);
    }

    public ObservableList<T> getItems() {
        return cbSort.getItems();
    }

    public boolean isReverse() {
        return reverse;
    }

    public void setReverse(boolean reverse) {
        if(this.reverse == reverse) return;
        this.reverse = reverse;
        if(reverse) {
            btSort.getStyleClass().add("reverse");
        } else {
            btSort.getStyleClass().remove("reverse");
        }
    }

    public void toggleReverse() {
        setReverse(!reverse);
    }

    public T getSelected() {
        return cbSort.getValue();
    }

    public void select(T item) {
        cbSort.getSelectionModel().select(item);
    }

    public void select(int index) {
        cbSort.getSelectionModel().select(index);
    }

    public void setOnSelectionChanged(ChangeListener<? super T> onChange) {
        cbSort.getSelectionModel().selectedItemProperty().removeListener(onChange);
        cbSort.getSelectionModel().selectedItemProperty().addListener(onChange);
    }
}
