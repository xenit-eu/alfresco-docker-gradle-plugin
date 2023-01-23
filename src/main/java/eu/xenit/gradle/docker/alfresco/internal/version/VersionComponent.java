package eu.xenit.gradle.docker.alfresco.internal.version;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class VersionComponent {

    private final String version;
    private final String propertyName;

    VersionComponent(@Nonnull String propertyName, @Nullable String version) {
        this.version = version;
        this.propertyName = propertyName;

    }

    @Nonnull
    public String getPropertyName() {
        return propertyName;
    }

    @Nullable
    public String getVersion() {
        return version;
    }

    @Nonnull
    public String getCheckCommand(String pathInContainer) {
        if (version == null) {
            return "true";
        }
        String cleanedFile = "cat " + pathInContainer + " | sed 's/\\r//'";
        String errorMessage =
                "echo \\\"Version mismatch: Expected " + propertyName + " to be $(" + cleanedFile+ " | grep -F '"+propertyName+"='| cut -d= -f2-) (image), but is " + version + " (base war)\\\" > /dev/stderr";
        String toFind = propertyName + '=' + version;

        return "bash -c \"if ! " + cleanedFile + " | grep -xqF '" + toFind + "'; then " + errorMessage
                + "; exit 1; fi \"";
    }
}
