package net.treset.minecraftlauncher;

public class Main {
    public static void main(String[] args) {
        /*LauncherFiles files = new LauncherFiles();
        files.reloadAll();
        Map<String, LauncherManifestType> typeConversion = files.getLauncherDetails().getTypeConversion();
        MinecraftVersionDetails mcVersion = MinecraftVersionDetails.fromJson(Sources.getFileFromUrl(VersionLoader.getReleases().get(0).getUrl()));
        InstanceCreator creator = new InstanceCreator(
                "testInstance",
                typeConversion,
                files.getInstanceManifest(),
                List.of("testfile"),
                List.of(),
                List.of(),
                new ModsCreator("testMods", typeConversion, files.getModsManifest(), "fabric", "1.19.4", files.getGameDetailsManifest()),
                new OptionsCreator("testOptions", typeConversion, files.getOptionsManifest()),
                new ResourcepackCreator("testResourcepacks", typeConversion, files.getResourcepackManifest()),
                new SavesCreator("testSaves", typeConversion, files.getSavesManifest(), files.getGameDetailsManifest()),
                new VersionCreator("testVersion", typeConversion, files.getVersionManifest(), mcVersion, files, Config.BASE_DIR + files.getLauncherDetails().getLibrariesDir())
        );
        String id = creator.createComponent();
        files.reloadAll();*/

        LauncherApplication.main(args);
    }
}
