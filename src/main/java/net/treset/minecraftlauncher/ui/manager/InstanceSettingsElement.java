package net.treset.minecraftlauncher.ui.manager;

import com.sun.management.OperatingSystemMXBean;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import net.treset.mc_version_loader.format.FormatUtils;
import net.treset.mc_version_loader.launcher.LauncherLaunchArgument;
import net.treset.minecraftlauncher.LauncherApplication;
import net.treset.minecraftlauncher.data.InstanceData;
import net.treset.minecraftlauncher.ui.base.UiElement;
import net.treset.minecraftlauncher.util.FormatUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class InstanceSettingsElement extends UiElement {
    @FXML private VBox root;
    @FXML private Slider slMemory;
    @FXML private TextField tfMemory;

    private InstanceData instanceData;

    private Integer oldValue;
    private Integer newValue;


    public void init(InstanceData instanceData) {
        this.instanceData = instanceData;
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
            this.newValue = newValue.intValue();
        });

        slMemory.setValue(getCurrentMemory());
        oldValue = (int)slMemory.getValue();
        tfMemory.setText(String.valueOf(oldValue));
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
        if(!Objects.equals(newValue, oldValue)) {
            ArrayList<LauncherLaunchArgument> newArguments = new ArrayList<>();
            for(LauncherLaunchArgument argument : instanceData.getInstance().getValue().getJvm_arguments()) {
                if(!argument.getArgument().startsWith("-Xmx") && !argument.getArgument().startsWith("-Xms")) {
                    newArguments.add(argument);
                }
            }
            newArguments.add(new LauncherLaunchArgument("-Xmx" + newValue + "m", null, null, null, null));
            newArguments.add(new LauncherLaunchArgument("-Xms" + newValue + "m", null, null, null, null));
            instanceData.getInstance().getValue().setJvm_arguments(newArguments);
            try {
                instanceData.getInstance().getValue().writeToFile(FormatUtil.absoluteFilePath(instanceData.getInstance().getKey().getDirectory(), instanceData.getInstance().getKey().getDetails()));
            } catch (IOException e) {
                LauncherApplication.displayError(e);
            }
            newValue = oldValue;
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
