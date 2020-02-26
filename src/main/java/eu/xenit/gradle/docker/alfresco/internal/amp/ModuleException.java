package eu.xenit.gradle.docker.alfresco.internal.amp;

import org.gradle.api.GradleException;

public class ModuleException extends GradleException {

    ModuleException(String message) {
        super(message);
    }

    ModuleException(String message, Throwable parent) {
        super(message, parent);
    }

}
