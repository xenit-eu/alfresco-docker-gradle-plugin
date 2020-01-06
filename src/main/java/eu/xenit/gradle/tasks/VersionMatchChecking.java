package eu.xenit.gradle.tasks;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.gradle.util.CollectionUtils;

class VersionMatchChecking {

    private static String buildCrashIfNotFoundOnceInVersionProperties(String toFind, String containerAlfrescoDirectory) {
        String versionPropertiesPath=containerAlfrescoDirectory+"alfresco/WEB-INF/classes/alfresco/version.properties";
        String NbVersionHits = "$(grep -cF '"+toFind+"' '"+versionPropertiesPath+"')";
        String crashOnMisMatch = "if [ 1 -eq " + NbVersionHits + " ]; then exit 0; else "+
                " exit 1;fi # Alfresco version mismatch between base image and base war";
        return crashOnMisMatch;
    }
    private final static String versionMajor = "version.major=";
    private final static String versionMinor = "version.minor=";
    private final static String versionRevision = "version.revision=";

    static List<String> getCanAddWarsCheckCommands(File explodedWarDir, String containerAlfrescoDirectory) {
        Path versionFilePath = explodedWarDir.toPath()
                .resolve("WEB-INF").resolve("classes").resolve("alfresco").resolve("version.properties");
        String version = Files.exists(versionFilePath) ? extractVersionFromVersionFile(versionFilePath) : null;
        if (version == null) {
            return Collections.emptyList();
        }
        String[] elements = version.split(",");
        String major = versionMajor + elements[0];
        String minor = versionMinor + elements[1];
        String revision = versionRevision + elements[2];
        return Arrays.asList(
                buildCrashIfNotFoundOnceInVersionProperties(major,containerAlfrescoDirectory),
                buildCrashIfNotFoundOnceInVersionProperties(minor,containerAlfrescoDirectory),
                buildCrashIfNotFoundOnceInVersionProperties(revision,containerAlfrescoDirectory));
    }

    private static String extractVersionFromVersionFile(Path path) {
        try {
            List<String> lines = FileUtils.readLines(path.toFile(), "UTF-8");
            String major = CollectionUtils.filter(lines, e -> e.startsWith(versionMajor)).get(0).substring(versionMajor.length());
            String minor = CollectionUtils.filter(lines, e -> e.startsWith(versionMinor)).get(0).substring(versionMinor.length());
            String patch = CollectionUtils.filter(lines, e -> e.startsWith(versionRevision)).get(0).substring(versionRevision.length());
            String version = String.format("%s,%s,%s", major, minor, patch);
            return version;
        } catch (Exception e) {
            throw new Error(e);
        }
    }



}
