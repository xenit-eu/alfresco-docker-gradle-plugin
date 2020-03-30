package eu.xenit.gradle.docker.tasks.internal;

import org.gradle.api.GradleException;

public class NoImagesConfiguredException extends GradleException {
    public NoImagesConfiguredException() {
        super("Pushing no docker images is not a valid action. Please configure some images to push.");
    }
}
