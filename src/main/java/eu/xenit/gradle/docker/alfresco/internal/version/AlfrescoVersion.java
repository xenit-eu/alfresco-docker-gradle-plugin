package eu.xenit.gradle.docker.alfresco.internal.version;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AlfrescoVersion {

    private static final String[] VERSION_COMPONENTS = {
            "version.major",
            "version.minor",
            "version.revision",
            "version.label",
            "version.edition"
    };

    private static final String[] VERSION_PROPERTIES_PATH = {"WEB-INF", "classes", "alfresco", "version.properties"};

    private final VersionComponent[] parts;

    @Nullable
    public static AlfrescoVersion fromAlfrescoWar(@Nonnull Path warPath) throws IOException {
        try(FileSystem zipFs = FileSystems.newFileSystem(warPath, AlfrescoVersion.class.getClassLoader())) {
            Path versionPropertiesPath = zipFs.getPath("/");
            for (String pathComponent : VERSION_PROPERTIES_PATH) {
                versionPropertiesPath = versionPropertiesPath.resolve(pathComponent);
            }
            return fromAlfrescoVersionProperties(versionPropertiesPath);
        }
    }

    @Nullable
    public static AlfrescoVersion fromAlfrescoVersionProperties(@Nonnull Path versionPropertiesPath)
            throws IOException {
        Properties versionProperties = new Properties();

        if (!Files.exists(versionPropertiesPath)) {
            return null;
        }

        try (InputStream versionStream = Files.newInputStream(versionPropertiesPath)) {
            versionProperties.load(versionStream);
        }
        return new AlfrescoVersion(versionProperties);
    }

    public AlfrescoVersion(@Nonnull Properties properties) {
        parts = new VersionComponent[VERSION_COMPONENTS.length];

        for (int i = 0; i < VERSION_COMPONENTS.length; i++) {
            parts[i] = new VersionComponent(VERSION_COMPONENTS[i], properties.getProperty(VERSION_COMPONENTS[i]));
        }
    }

    @Nonnull
    public String getVersion() {
        return Arrays.stream(parts)
                .map(VersionComponent::getVersion)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("."));
    }

    @Nonnull
    public String getCheckCommand(@Nonnull String pathInContainer) {
        return Arrays.stream(parts)
                .map(part -> part.getCheckCommand(pathInContainer + "/" + String.join("/", VERSION_PROPERTIES_PATH)))
                .collect(Collectors.joining(" && ")) + " # Alfresco version mismatch between base image and base war";
    }

}
