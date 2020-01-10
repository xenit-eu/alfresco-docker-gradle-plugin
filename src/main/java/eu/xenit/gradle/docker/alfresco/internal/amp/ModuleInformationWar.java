package eu.xenit.gradle.docker.alfresco.internal.amp;

import java.io.File;
import org.alfresco.service.cmr.module.ModuleDetails;

class ModuleInformationWar extends ModuleInformationFromModuleDetails {

    private ModuleDetails moduleDetails;

    public ModuleInformationWar(ModuleDetails moduleDetails) {
        this.moduleDetails = moduleDetails;
    }

    @Override
    public ModuleDetails getModuleDetails() {
        return moduleDetails;
    }

    @Override
    public File getFile() {
        return null;
    }
}
