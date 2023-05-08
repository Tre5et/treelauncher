package net.treset.minecraftlauncher.creation;

import net.treset.mc_version_loader.files.JavaFileDownloader;
import net.treset.mc_version_loader.files.Sources;
import net.treset.mc_version_loader.java.JavaFile;
import net.treset.mc_version_loader.java.JavaRuntime;
import net.treset.mc_version_loader.java.JavaRuntimeOs;
import net.treset.mc_version_loader.java.JavaRuntimeRelease;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.mc_version_loader.os.OsDetails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;

public class JavaComponentCreator extends GenericComponentCreator {
    private static final Logger LOGGER = LogManager.getLogger(JavaComponentCreator.class);

    public JavaComponentCreator(String name, Map<String, LauncherManifestType> typeConversion, LauncherManifest componentsManifest) {
        super(LauncherManifestType.JAVA_COMPONENT, null, null, name, typeConversion, null, null, componentsManifest);
    }

    @Override
    public String createComponent() {
        String result = super.createComponent();

        if(result == null || getNewManifest() == null) {
            LOGGER.warn("Unable to create java component: invalid data");
            return null;
        }

        JavaRuntime java = JavaRuntime.fromJson(Sources.getJavaRuntimeJson());
        if(java.getRuntimes() == null) {
            LOGGER.warn("Unable to create java component: failed to get java runtime");
            return null;
        }

        String osIdentifier = OsDetails.getJavaIdentifier();
        if(osIdentifier == null) {
            LOGGER.warn("Unable to create java component: failed to get os identifier");
            return null;
        }

        JavaRuntimeOs os = null;
        for(JavaRuntimeOs o : java.getRuntimes()) {
            if(o.getId().equals(osIdentifier)) {
                os = o;
                break;
            }
        }
        if(os == null || os.getReleases() == null) {
            LOGGER.warn("Unable to create java component: failed to get os runtime");
            return null;
        }

        JavaRuntimeRelease release = null;
        for(JavaRuntimeRelease r : os.getReleases()) {
            if(r != null && getName().equals(r.getId())) {
                release = r;
                break;
            }
        }

        if(release == null || release.getManifest() == null || release.getManifest().getUrl() == null) {
            LOGGER.warn("Unable to create java component: failed to get release");
            return null;
        }

        List<JavaFile> files = JavaFile.fromJsonManifest(Sources.getFileFromUrl(release.getManifest().getUrl()));

        File baseDir = new File(getNewManifest().getDirectory());
        if(!baseDir.isDirectory()) {
            LOGGER.warn("Unable to create java component: base dir is not a directory");
            return null;
        }

        for(JavaFile f : files) {
            if(!JavaFileDownloader.downloadJavaFile(f, baseDir)) {
                LOGGER.warn("Unable to create java component: failed to download file: name={}", f.getName());
                return null;
            }
        }

        return result;
    }

    @Override
    public String useComponent() {
        LOGGER.warn("Unable to use java component: unsupported");
        return null;
    }

    @Override
    public String inheritComponent() {
        LOGGER.warn("Unable to inherit java component: unsupported");
        return null;
    }
}
