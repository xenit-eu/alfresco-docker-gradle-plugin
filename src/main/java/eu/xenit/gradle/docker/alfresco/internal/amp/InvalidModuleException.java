package eu.xenit.gradle.docker.alfresco.internal.amp;

import java.nio.file.Path;

public class InvalidModuleException extends ModuleException {

    InvalidModuleException(Path path, Throwable parent) {
        super(path + " is not a valid AMP", parent);
    }
}
