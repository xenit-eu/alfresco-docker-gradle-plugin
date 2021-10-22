package eu.xenit.gradle.docker.alfresco.internal.amp;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import org.alfresco.error.AlfrescoRuntimeException;

public class InvalidModuleException extends ModuleException {

    InvalidModuleException(Path path, Throwable parent) {
        super(createMessage(path, parent), parent);
    }

    InvalidModuleException(Path path, UncheckedIOException parent) {
        super(createMessage(path, parent.getCause()), parent);
    }

    private static String createMessage(Path path, Throwable parent) {
        return path + " is not a valid AMP: " + parent.toString();
    }

    InvalidModuleException(Path path, AlfrescoRuntimeException parent) {
        super(path+" is not a valid AMP: "+parent.getMsgId(), parent);
    }
}
