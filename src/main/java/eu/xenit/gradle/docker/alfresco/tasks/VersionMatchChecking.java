package eu.xenit.gradle.docker.alfresco.tasks;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.gradle.util.CollectionUtils;

final class VersionMatchChecking {

    private VersionMatchChecking() {
    }

    private static String buildCrashIfNotFoundOnceInVersionProperties(String toFind,
            String containerAlfrescoDirectory) {
        String versionPropertiesPath =
                containerAlfrescoDirectory + "alfresco/WEB-INF/classes/alfresco/version.properties";
        String nbVersionHits = "$(grep -cF '" + toFind + "' '" + versionPropertiesPath + "')";
        return "if [ 1 -eq " + nbVersionHits + " ]; then exit 0; else " +
                " exit 1;fi # Alfresco version mismatch between base image and base war";
    }

    private static final String VERSION_MAJOR = "version.major=";
    private static final String VERSION_MINOR = "version.minor=";
    private static final String VERSION_REVISION = "version.revision=";

    static List<String> getCanAddWarsCheckCommands(File explodedWarDir, String containerAlfrescoDirectory) {
        Path versionFilePath = explodedWarDir.toPath()
                .resolve("WEB-INF").resolve("classes").resolve("alfresco").resolve("version.properties");
        String[] version = Files.exists(versionFilePath) ? extractVersionFromVersionFile(versionFilePath) : null;
        if (version == null) {
            return Collections.emptyList();
        }
        String major = VERSION_MAJOR + version[0];
        String minor = VERSION_MINOR + version[1];
        String revision = VERSION_REVISION + version[2];
        return Arrays.asList(
                buildCrashIfNotFoundOnceInVersionProperties(major, containerAlfrescoDirectory),
                buildCrashIfNotFoundOnceInVersionProperties(minor, containerAlfrescoDirectory),
                buildCrashIfNotFoundOnceInVersionProperties(revision, containerAlfrescoDirectory)
        );
    }

    private static String[] extractVersionFromVersionFile(Path path) {
        try {
            List<String> lines = FileUtils.readLines(path.toFile(), StandardCharsets.UTF_8);
            String major = CollectionUtils.filter(lines, e -> e.startsWith(VERSION_MAJOR)).get(0).substring(
                    VERSION_MAJOR.length());
            String minor = CollectionUtils.filter(lines, e -> e.startsWith(VERSION_MINOR)).get(0).substring(
                    VERSION_MINOR.length());
            String patch = CollectionUtils.filter(lines, e -> e.startsWith(VERSION_REVISION)).get(0).substring(
                    VERSION_REVISION.length());
            return new String[]{major, minor, patch};
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
