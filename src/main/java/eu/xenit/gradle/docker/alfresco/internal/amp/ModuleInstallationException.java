package eu.xenit.gradle.docker.alfresco.internal.amp;

import java.io.File;
import org.alfresco.error.AlfrescoRuntimeException;

public class ModuleInstallationException extends ModuleException {

    public ModuleInstallationException(ModuleInformation moduleInformation, Throwable parent) {
        super(createModuleMessage(moduleInformation) + parent.getMessage(), parent);
    }

    public ModuleInstallationException(ModuleInformation moduleInformation,
            AlfrescoRuntimeException alfrescoException) {
        super(createModuleMessage(moduleInformation) + alfrescoException.getMessage().split(" ", 2)[1],
                alfrescoException);
    }

    private static String createModuleMessage(ModuleInformation moduleInformation) {
        return "Failed to install module " + moduleInformation.getId() + " (" + moduleInformation.getFile().getPath()
                + "): ";
    }
}
