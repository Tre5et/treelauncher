package net.treset.minecraftlauncher.ui.manager;

import com.sun.management.OperatingSystemMXBean;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import net.treset.mc_version_loader.format.FormatUtils;
import net.treset.mc_version_loader.launcher.LauncherFeature;
import net.treset.mc_version_loader.launcher.LauncherLaunchArgument;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.ui.generic.PopupElement;
import net.treset.minecraftlauncher.util.FormatUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class InstanceSettingsElement extends UiElement {
    @FXML private VBox root;
    @FXML private Slider slMemory;
    @FXML private TextField tfMemory;
    @FXML private TextField tfWidth;
    @FXML private TextField tfHeight;
    @FXML private ListView<String> lvArguments;

    private InstanceData instanceData;

    private Integer oldMemory;
    private Integer newMemory;

    private String oldWidth;
    private String oldHeight;


    public void init(InstanceData instanceData) {
        this.instanceData = instanceData;

        initMemory();

        initResolution();

        initArguments();
    }

    private void initArguments() {
        lvArguments.getItems().clear();
        lvArguments.getItems().addAll(instanceData.getInstance().getValue().getJvm_arguments().stream().map(LauncherLaunchArgument::getArgument).filter(a -> !a.startsWith("-Xmx") && !a.startsWith("-Xms")).toList());
    }

    private void initResolution() {
        List<LauncherFeature> features = instanceData.getInstance().getValue().getFeatures();
        Optional<LauncherFeature> resX = features.stream().filter(feature -> feature.getFeature().equals("resolution_x")).findFirst();
        Optional<LauncherFeature> resY = features.stream().filter(feature -> feature.getFeature().equals("resolution_y")).findFirst();

        setPixelFormatter(tfWidth);
        setPixelFormatter(tfHeight);

        if(resX.isPresent()) {
            tfWidth.setText(resX.get().getValue());
            oldWidth = resX.get().getValue();
        } else {
            tfWidth.setText("854");
            oldWidth = "854";
        }

        if(resY.isPresent()) {
            tfHeight.setText(resY.get().getValue());
            oldHeight = resY.get().getValue();
        } else {
            tfHeight.setText("480");
            oldHeight = "480";
        }

    }

    private void setPixelFormatter(TextField tf) {
        tf.setTextFormatter(new TextFormatter<>(new StringConverter<Integer>() {
            @Override
            public String toString(Integer value) {
                if (value == null) {
                    return "";
                }

                return String.valueOf(value);
            }

            @Override
            public Integer fromString(String string) {
                if (string == null || string.isEmpty()) {
                    return null;
                }

                return Integer.valueOf(string);
            }
        }));
    }

    private void initMemory() {
        slMemory.setLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Double value) {
                return value.intValue() + "mb";
            }

            @Override
            public Double fromString(String string) {
                return Double.parseDouble(string.replace("mb", ""));
            }
        });

        slMemory.setMax(getSystemMemory());
        slMemory.setMajorTickUnit(slMemory.getMax() - slMemory.getMin());
        slMemory.setBlockIncrement(1024);
        tfMemory.setTextFormatter(new TextFormatter<>(new StringConverter<Integer>() {
            @Override
            public String toString(Integer value) {
                if (value == null) {
                    return "";
                }

                return (value + "mb");
            }

            @Override
            public Integer fromString(String string) {
                if (string == null) {
                    return null;
                }

                string = string.trim().replace("mb", "");

                if (string.isEmpty()) {
                    return null;
                }

                return Integer.valueOf(string);
            }
        }));
        slMemory.valueProperty().addListener((observable, oldValue, newValue) -> {
            tfMemory.setText(String.valueOf(newValue.intValue()));
            this.newMemory = newValue.intValue();
        });

        slMemory.setValue(getCurrentMemory());
        oldMemory = (int)slMemory.getValue();
        tfMemory.setText(String.valueOf(oldMemory));
    }

    public int getCurrentMemory() {
        for(LauncherLaunchArgument argument : instanceData.getInstance().getValue().getJvm_arguments()) {
            if((argument.getArgument().startsWith("-Xmx") || argument.getArgument().startsWith("-Xms")) && argument.getArgument().endsWith("m")) {
                return Integer.parseInt(argument.getArgument().replace("-Xmx", "").replaceAll("m", ""));
            }
        }

        try {
            String regex = "MaxHeapSize\\s*=\\s?(\\d+)";
            String result = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(FormatUtil.absoluteFilePath(instanceData.getJavaComponent().getDirectory(), "bin", "java") + " -XX:+PrintFlagsFinal -version | findstr HeapSize").getInputStream())).lines().collect(Collectors.joining(" "));
            if(FormatUtils.matches(result, regex)) {
                return (int)(Long.parseLong(FormatUtils.firstGroup(result, regex)) / 1024 / 1024);
            }
            LauncherApplication.displayError(new Exception("Could not determine default java memory"));
        } catch (IOException e) {
            LauncherApplication.displayError(e);
        }

        return 1024;
    }

    public int getSystemMemory() {
        OperatingSystemMXBean bean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        return (int)(bean.getTotalMemorySize() / 1024 / 1024);
    }

    public void save() {
        saveMemory();
        saveResolution();
        saveArguments();
    }

    private void saveMemory() {
        if(!Objects.equals(newMemory, oldMemory)) {
            ArrayList<LauncherLaunchArgument> newArguments = new ArrayList<>();
            for(LauncherLaunchArgument argument : instanceData.getInstance().getValue().getJvm_arguments()) {
                if(!argument.getArgument().startsWith("-Xmx") && !argument.getArgument().startsWith("-Xms")) {
                    newArguments.add(argument);
                }
            }
            newArguments.add(new LauncherLaunchArgument("-Xmx" + newMemory + "m", null, null, null, null));
            newArguments.add(new LauncherLaunchArgument("-Xms" + newMemory + "m", null, null, null, null));
            instanceData.getInstance().getValue().setJvm_arguments(newArguments);
            try {
                instanceData.getInstance().getValue().writeToFile(FormatUtil.absoluteFilePath(instanceData.getInstance().getKey().getDirectory(), instanceData.getInstance().getKey().getDetails()));
            } catch (IOException e) {
                LauncherApplication.displayError(e);
            }
            newMemory = oldMemory;
        }
    }

    private void saveResolution() {
        String width = tfWidth.getText().replace(" px", "");
        String height = tfHeight.getText().replace(" px", "");
        if(width.equals(oldWidth) && height.equals(oldHeight)) {
            return;
        }
        ArrayList<LauncherFeature> newFeatures = new ArrayList<>(instanceData.getInstance().getValue().getFeatures().stream().filter(f -> !Objects.equals(f.getFeature(), "resolution_x") && !Objects.equals(f.getFeature(), "resolution_y")).toList());
        newFeatures.add(new LauncherFeature("resolution_x", tfWidth.getText().replace(" px", "")));
        newFeatures.add(new LauncherFeature("resolution_y", tfHeight.getText().replace(" px", "")));
        instanceData.getInstance().getValue().setFeatures(newFeatures);
        try {
            instanceData.getInstance().getValue().writeToFile(FormatUtil.absoluteFilePath(instanceData.getInstance().getKey().getDirectory(), instanceData.getInstance().getKey().getDetails()));
        } catch (IOException e) {
            LauncherApplication.displayError(e);
        }
        oldWidth = width;
        oldHeight = height;
    }

    private void saveArguments() {
        ArrayList<LauncherLaunchArgument> arguments = new ArrayList<>(lvArguments.getItems().stream().map(a -> new LauncherLaunchArgument(a, null, null, null, null)).toList());
        for(LauncherLaunchArgument argument : instanceData.getInstance().getValue().getJvm_arguments()) {
            if(argument.getArgument().startsWith("-Xmx") || argument.getArgument().startsWith("-Xms")) {
                arguments.add(argument);
            }
        }
        instanceData.getInstance().getValue().setJvm_arguments(arguments);
        try {
            instanceData.getInstance().getValue().writeToFile(FormatUtil.absoluteFilePath(instanceData.getInstance().getKey().getDirectory(), instanceData.getInstance().getKey().getDetails()));
        } catch (IOException e) {
            LauncherApplication.displayError(e);
        }
    }

    @FXML
    private void onAdd() {
        PopupElement.PopupTextInput input = new PopupElement.PopupTextInput("instances.settings.popup.prompt.argument");

        LauncherApplication.setPopup(
                new PopupElement(
                        PopupElement.PopupType.NONE,
                        "instances.settings.popup.argument",
                        "",
                        List.of(input),
                        List.of(
                                new PopupElement.PopupButton(PopupElement.ButtonType.NEGATIVE, "instances.settings.popup.cancel", event -> LauncherApplication.setPopup(null)),
                                new PopupElement.PopupButton(PopupElement.ButtonType.POSITIVE, "instances.settings.popup.confirm", event -> {
                                    String text = input.getText();
                                    if(text != null && !text.isEmpty()) {
                                        lvArguments.getItems().add(text);
                                    }
                                    LauncherApplication.setPopup(null);
                                })
                        )
                )
        );
    }

    @FXML
    private void onRemove() {
        String selected = lvArguments.getSelectionModel().getSelectedItem();
        if(selected != null) {
            lvArguments.getItems().remove(selected);
        }
    }

    @Override
    public void beforeShow(Stage stage) {

    }

    @Override
    public void afterShow(Stage stage) {

    }

    @Override
    public void setRootVisible(boolean visible) {
        root.setVisible(visible);
        if(!visible) {
            save();
        }
    }
}
