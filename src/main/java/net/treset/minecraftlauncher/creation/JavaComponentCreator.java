package net.treset.minecraftlauncher.creation;

import net.treset.mc_version_loader.exception.FileDownloadException;
import net.treset.mc_version_loader.files.JavaFileDownloader;
import net.treset.mc_version_loader.files.Sources;
import net.treset.mc_version_loader.java.JavaFile;
import net.treset.mc_version_loader.java.JavaRuntime;
import net.treset.mc_version_loader.java.JavaRuntimeOs;
import net.treset.mc_version_loader.java.JavaRuntimeRelease;
import net.treset.mc_version_loader.launcher.LauncherManifest;
import net.treset.mc_version_loader.launcher.LauncherManifestType;
import net.treset.mc_version_loader.os.OsDetails;
import net.treset.minecraftlauncher.util.CreationStatus;
import net.treset.minecraftlauncher.util.exception.ComponentCreationException;

import java.io.File;
import java.util.List;
import java.util.Map;

public class JavaComponentCreator extends GenericComponentCreator {
    public JavaComponentCreator(String name, Map<String, LauncherManifestType> typeConversion, LauncherManifest componentsManifest) {
        super(LauncherManifestType.JAVA_COMPONENT, null, null, name, typeConversion, null, null, componentsManifest);
        setDefaultStatus(new CreationStatus(CreationStatus.DownloadStep.JAVA, null));
    }

    @Override
    public String createComponent() throws ComponentCreationException {
        String result = super.createComponent();

        if(result == null || getNewManifest() == null) {
            attemptCleanup();
            throw new ComponentCreationException("Failed to create java component: invalid data");
        }

        JavaRuntime java;
        try {
            java = JavaRuntime.fromJson(Sources.getJavaRuntimeJson());
        } catch (FileDownloadException e) {
            attemptCleanup();
            throw new ComponentCreationException("Failed to create java component: failed to download java runtime json", e);
        }

        String osIdentifier;
        try {
            osIdentifier = OsDetails.getJavaIdentifier();
        } catch (IllegalArgumentException e) {
            attemptCleanup();
            throw new ComponentCreationException("Failed to create java component: failed to get os identifier", e);
        }

        JavaRuntimeOs os = null;
        for(JavaRuntimeOs o : java.getRuntimes()) {
            if(o.getId().equals(osIdentifier)) {
                os = o;
                break;
            }
        }
        if(os == null || os.getReleases() == null) {
            attemptCleanup();
            throw new ComponentCreationException("Failed to create java component: failed to get os runtime");
        }

        JavaRuntimeRelease release = null;
        for(JavaRuntimeRelease r : os.getReleases()) {
            if(r != null && getName().equals(r.getId())) {
                release = r;
                break;
            }
        }

        if(release == null || release.getManifest() == null || release.getManifest().getUrl() == null) {
            attemptCleanup();
            throw new ComponentCreationException("Failed to create java component: failed to get release");
        }

        List<JavaFile> files;
        try {
            files = JavaFile.fromJsonManifest(Sources.getFileFromUrl(release.getManifest().getUrl()));
        } catch (FileDownloadException e) {
            throw new ComponentCreationException("Failed to create java component: failed to download java file manifest", e);
        }

        File baseDir = new File(getNewManifest().getDirectory());

        try {
            JavaFileDownloader.downloadJavaFiles(baseDir, files, status -> setStatus(new CreationStatus(CreationStatus.DownloadStep.JAVA, status)));
        } catch (FileDownloadException e) {
            throw new ComponentCreationException("Failed to create java component: failed to download java files", e);
        }

        return result;
    }

    @Override
    public String useComponent() throws ComponentCreationException {
        attemptCleanup();
        throw new ComponentCreationException("Unable to use java component: unsupported");
    }

    @Override
    public String inheritComponent() throws ComponentCreationException {
        attemptCleanup();
        throw new ComponentCreationException("Unable to inherit java component: unsupported");
    }
}
