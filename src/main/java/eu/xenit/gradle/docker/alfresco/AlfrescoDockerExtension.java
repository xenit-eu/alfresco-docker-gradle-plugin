package eu.xenit.gradle.docker.alfresco;

import org.gradle.api.provider.Property;

public abstract class AlfrescoDockerExtension {
    public abstract Property<String> getBaseImage();
    public abstract Property<Boolean> getLeanImage();
}
