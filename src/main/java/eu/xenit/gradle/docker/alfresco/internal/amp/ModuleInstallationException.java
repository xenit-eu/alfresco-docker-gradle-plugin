package eu.xenit.gradle.docker.alfresco.internal.amp;

import java.io.File;
import org.alfresco.error.AlfrescoRuntimeException;

public class ModuleInstallationException extends ModuleException {

    public ModuleInstallationException(ModuleInformation moduleInformation, Throwable parent) {
        this(moduleInformation, parent, parent);
    }

    public ModuleInstallationException(ModuleInformation moduleInformation, Throwable messageParent, Throwable realParent) {
        super("Failed to install module " + moduleInformation.getId() + " (" + moduleInformation.getFile().getPath()+"): "+createParentMessage(messageParent), realParent);
    }

    private static String createParentMessage(Throwable parent) {
        if(parent instanceof AlfrescoRuntimeException) {
            return parent.getMessage().split(" ", 2)[1];
        }
        return parent.getMessage();
    }
}
