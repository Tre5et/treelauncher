package net.treset.minecraftlauncher.file_loading;

import javafx.util.Pair;
import net.treset.mc_version_loader.launcher.LauncherInstanceDetails;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherModsDetails;
import net.treset.mc_version_loader.launcher.LauncherVersionDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InstanceData {
    private static Logger LOGGER = Logger.getLogger(InstanceData.class.getName());

    private Pair<LauncherManifest, LauncherInstanceDetails> instance;
    private List<Pair<LauncherManifest, LauncherVersionDetails>> versionComponents;
    private LauncherManifest javaComponent = null;
    LauncherManifest optionsComponent = null;
    LauncherManifest resourcepacksComponent = null;
    LauncherManifest savesComponent = null;
    Pair<LauncherManifest, LauncherModsDetails> modsComponent = null;

    public static InstanceData of(Pair<LauncherManifest, LauncherInstanceDetails> instance, LauncherFiles files) {
        if(!files.reloadAll()) {
            LOGGER.log(Level.WARNING, "Unable to prepare launch resources: file reload failed");
            return null;
        }

        List<Pair<LauncherManifest, LauncherVersionDetails>> versionComponents = new ArrayList<>();
        Pair<LauncherManifest, LauncherVersionDetails> currentComponent = null;
        for (Pair<LauncherManifest, LauncherVersionDetails> v : files.getVersionComponents()) {
            if (Objects.equals(v.getKey().getId(), instance.getValue().getVersionComponent())) {
                currentComponent = v;
                break;
            }
        }
        if(currentComponent == null) {
            LOGGER.log(Level.WARNING, "Unable to prepare launch resources: unable to find version component: versionId=" + instance.getValue().getVersionComponent());
            return null;
        }
        versionComponents.add(currentComponent);

        while(currentComponent.getValue().getDepends() != null && !currentComponent.getValue().getDepends().isBlank()) {
            boolean found = false;
            for (Pair<LauncherManifest, LauncherVersionDetails> v : files.getVersionComponents()) {
                if (Objects.equals(v.getKey().getId(), currentComponent.getValue().getDepends())) {
                    currentComponent = v;
                    found = true;
                    break;
                }
            }
            if(!found) {
                LOGGER.log(Level.WARNING, "Unable to prepare launch resources: unable to find dependent version component");
                return null;
            }
            versionComponents.add(currentComponent);
        }


        LauncherManifest javaComponent = null;
        for(Pair<LauncherManifest, LauncherVersionDetails> v : versionComponents) {
            if(v.getValue().getJava() != null && !v.getValue().getJava().isBlank()) {
                for (LauncherManifest j : files.getJavaComponents()) {
                    if (Objects.equals(j.getId(), v.getValue().getJava())) {
                        javaComponent = j;
                        break;
                    }
                }
                break;
            }
        }
        if(javaComponent == null) {
            LOGGER.log(Level.WARNING, "Unable to prepare launch resources: unable to find suitable java component");
            return null;
        }
        LauncherManifest optionsComponent = null;
        for(LauncherManifest o : files.getOptionsComponents()) {
            if(Objects.equals(o.getId(), instance.getValue().getOptionsComponent())) {
                optionsComponent = o;
                break;
            }
        }
        if(optionsComponent == null) {
            LOGGER.log(Level.WARNING, "Unable to prepare launch resources: unable to find options component: optionsId=" + instance.getValue().getOptionsComponent());
            return null;
        }
        LauncherManifest resourcepacksComponent = null;
        for(LauncherManifest r : files.getResourcepackComponents()) {
            if(Objects.equals(r.getId(), instance.getValue().getResourcepacksComponent())) {
                resourcepacksComponent = r;
                break;
            }
        }
        if(resourcepacksComponent == null) {
            LOGGER.log(Level.WARNING, "Unable to prepare launch resources: unable to find resourcepacks component: resourcepacksId=" + instance.getValue().getResourcepacksComponent());
            return null;
        }
        LauncherManifest savesComponent = null;
        for(LauncherManifest s : files.getSavesComponents()) {
            if(Objects.equals(s.getId(), instance.getValue().getSavesComponent())) {
                savesComponent = s;
                break;
            }
        }
        if(savesComponent == null) {
            LOGGER.log(Level.WARNING, "Unable to prepare launch resources: unable to find saves component: savesId=" + instance.getValue().getSavesComponent());
            return null;
        }
        Pair<LauncherManifest, LauncherModsDetails> modsComponent = null;
        if(instance.getValue().getModsComponent() != null && !instance.getValue().getModsComponent().isBlank()) {
            for(Pair<LauncherManifest, LauncherModsDetails> m : files.getModsComponents()) {
                if(Objects.equals(m.getKey().getId(), instance.getValue().getModsComponent())) {
                    modsComponent = m;
                    break;
                }
            }
            if(modsComponent == null) {
                LOGGER.log(Level.WARNING, "Unable to prepare launch resources: unable to find mods component: modsId=" + instance.getValue().getModsComponent());
                return null;
            }
        }

        return new InstanceData(
                instance,
                versionComponents,
                javaComponent,
                optionsComponent,
                resourcepacksComponent,
                savesComponent,
                modsComponent
        );
    }

    public InstanceData(Pair<LauncherManifest, LauncherInstanceDetails> instance, List<Pair<LauncherManifest, LauncherVersionDetails>> versionComponents, LauncherManifest javaComponent, LauncherManifest optionsComponent, LauncherManifest resourcepacksComponent, LauncherManifest savesComponent, Pair<LauncherManifest, LauncherModsDetails> modsComponent) {
        this.instance = instance;
        this.versionComponents = versionComponents;
        this.javaComponent = javaComponent;
        this.optionsComponent = optionsComponent;
        this.resourcepacksComponent = resourcepacksComponent;
        this.savesComponent = savesComponent;
        this.modsComponent = modsComponent;
    }

    public Pair<LauncherManifest, LauncherInstanceDetails> getInstance() {
        return instance;
    }

    public void setInstance(Pair<LauncherManifest, LauncherInstanceDetails> instance) {
        this.instance = instance;
    }
    public List<Pair<LauncherManifest, LauncherVersionDetails>> getVersionComponents() {
        return versionComponents;
    }

    public void setVersionComponents(List<Pair<LauncherManifest, LauncherVersionDetails>> versionComponents) {
        this.versionComponents = versionComponents;
    }

    public LauncherManifest getJavaComponent() {
        return javaComponent;
    }

    public void setJavaComponent(LauncherManifest javaComponent) {
        this.javaComponent = javaComponent;
    }

    public LauncherManifest getOptionsComponent() {
        return optionsComponent;
    }

    public void setOptionsComponent(LauncherManifest optionsComponent) {
        this.optionsComponent = optionsComponent;
    }

    public LauncherManifest getResourcepacksComponent() {
        return resourcepacksComponent;
    }

    public void setResourcepacksComponent(LauncherManifest resourcepacksComponent) {
        this.resourcepacksComponent = resourcepacksComponent;
    }

    public LauncherManifest getSavesComponent() {
        return savesComponent;
    }

    public void setSavesComponent(LauncherManifest savesComponent) {
        this.savesComponent = savesComponent;
    }

    public Pair<LauncherManifest, LauncherModsDetails> getModsComponent() {
        return modsComponent;
    }

    public void setModsComponent(Pair<LauncherManifest, LauncherModsDetails> modsComponent) {
        this.modsComponent = modsComponent;
    }
}
