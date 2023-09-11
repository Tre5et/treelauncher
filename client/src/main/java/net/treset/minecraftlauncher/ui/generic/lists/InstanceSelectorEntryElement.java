package net.treset.minecraftlauncher.ui.generic.lists;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.util.FormatUtil;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.function.BiConsumer;

public class InstanceSelectorEntryElement extends SelectorEntryElement<InstanceSelectorEntryElement.InstanceDataContentProvider> {
    public static class InstanceDataContentProvider implements ContentProvider {
        private InstanceData data;
        public InstanceDataContentProvider(InstanceData data) {
            this.data = data;
        }

        public InstanceData getData() {
            return data;
        }

        public void setData(InstanceData data) {
            this.data = data;
        }

        @Override
        public String getTitle() {
            return getData().getInstance().getKey().getName();
        }

        @Override
        public String getDetails() {
            return getData().getVersionComponents().get(0).getValue().getVersionId();
        }
    }

    private InstanceData instanceData;

    private final HBox hbTime = new HBox();
    private final FontIcon icTime = new FontIcon();
    private final Label lbTime = new Label();

    public InstanceSelectorEntryElement(InstanceData data, BiConsumer<InstanceData, Boolean> selectionListener) {
        super(new InstanceDataContentProvider(data), (dataProvider, select) -> selectionListener.accept(dataProvider.getData(), select));
        this.instanceData = data;
        updateTime();

        this.getStylesheets().add("/css/generic/InstanceSelectorEntryElement.css");

        icTime.getStyleClass().add("time");

        lbTime.getStyleClass().add("hint");

        hbTime.setAlignment(Pos.TOP_RIGHT);
        hbTime.setPadding(new Insets(5));
        hbTime.setSpacing(3);
        hbTime.getChildren().addAll(icTime, lbTime);
        hbTime.setMouseTransparent(true);

        this.getChildren().add(hbTime);
    }

    public void updateTime() {
        setTime(instanceData.getInstance().getValue().getTotalTime());
    }

    public void setTime(long seconds) {
        lbTime.setText(FormatUtil.formatSeconds(seconds));
    }

    public InstanceData getInstanceData() {
        return instanceData;
    }

    public void setInstanceData(InstanceData instanceData) {
        this.instanceData = instanceData;
        setContentProvider(new InstanceDataContentProvider(instanceData));
        setTime(instanceData.getInstance().getValue().getTotalTime());
    }
}
