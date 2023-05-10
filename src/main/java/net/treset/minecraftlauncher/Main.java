package net.treset.minecraftlauncher;

public class Main {
    public static void main(String[] args) {
        /*LauncherFiles files = new LauncherFiles();
        files.reloadAll();
        Map<String, LauncherManifestType> typeConversion = files.getLauncherDetails().getTypeConversion();
        String fabrics = Sources.getFabricForMinecraftVersion("1.19.4");
        List<FabricVersionDetails> fabricVersions = FabricVersionDetails.fromJsonArray(fabrics);
        FabricProfile fabricProfile = FabricProfile.fromJson(Sources.getFileFromHttpGet("https://meta.fabricmc.net/v2/versions/loader/1.19.4/" + fabricVersions.get(0).getLoader().getVersion() + "/profile/json", List.of(), List.of()));


        MinecraftVersionDetails mcVersion = MinecraftVersionDetails.fromJson(Sources.getFileFromUrl(VersionLoader.getReleases().get(0).getUrl()));
        InstanceCreator creator = new InstanceCreator(
                "testInstance3",
                typeConversion,
                files.getInstanceManifest(),
                List.of("testfile"),
                List.of(),
                List.of(),
                new ModsCreator(files.getModsComponents().get(0)),
                new OptionsCreator(files.getOptionsComponents().get(0)),
                new ResourcepackCreator(files.getResourcepackComponents().get(0)),
                new SavesCreator(files.getSavesComponents().get(0)),
                new VersionCreator(typeConversion, files.getVersionManifest(), fabricVersions.get(0), fabricProfile, files, Config.BASE_DIR + files.getLauncherDetails().getLibrariesDir())
        );
        String id = creator.getId();
        files.reloadAll();*/

        LauncherApplication.main(args);
    }
}
