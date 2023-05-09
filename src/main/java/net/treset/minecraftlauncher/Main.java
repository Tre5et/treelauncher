package net.treset.minecraftlauncher;

import net.treset.mc_version_loader.VersionLoader;
import net.treset.mc_version_loader.files.Sources;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.mc_version_loader.minecraft.MinecraftVersionDetails;
import net.treset.minecraftlauncher.config.Config;
import net.treset.minecraftlauncher.creation.*;
import net.treset.minecraftlauncher.data.LauncherFiles;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        LauncherFiles files = new LauncherFiles();
        files.reloadAll();
        Map<String, LauncherManifestType> typeConversion = files.getLauncherDetails().getTypeConversion();
        MinecraftVersionDetails mcVersion = MinecraftVersionDetails.fromJson(Sources.getFileFromUrl(VersionLoader.getReleases().get(0).getUrl()));
        InstanceCreator creator = new InstanceCreator(
                "testInstance2",
                typeConversion,
                files.getInstanceManifest(),
                List.of("testfile"),
                List.of(),
                List.of(),
                new ModsCreator(files.getModsComponents().get(0)),
                new OptionsCreator(files.getOptionsComponents().get(0)),
                new ResourcepackCreator(files.getResourcepackComponents().get(0)),
                new SavesCreator(files.getSavesComponents().get(0)),
                new VersionCreator("version1", typeConversion, files.getVersionManifest(), mcVersion, files, Config.BASE_DIR + files.getLauncherDetails().getLibrariesDir())
        );
        String id = creator.getId();
        files.reloadAll();

        LauncherApplication.main(args);
    }
}
