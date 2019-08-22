package eu.xenit.gradle.alfresco.amp;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.alfresco.service.cmr.module.ModuleDependency;
import org.alfresco.service.cmr.module.ModuleDetails;

abstract class ModuleInformationFromModuleDetails implements ModuleInformation {

    public abstract ModuleDetails getModuleDetails();

    @Override
    public String getId() {
        return getModuleDetails().getId();
    }

    @Override
    public Set<String> getIds() {
        Set<String> moduleIds = new HashSet<>(getModuleDetails().getAliases());
        moduleIds.add(getId());
        return Collections.unmodifiableSet(moduleIds);
    }

    @Override
    public Set<String> getDependencyModuleIds() {
        return getModuleDetails().getDependencies().stream()
                .map(ModuleDependency::getDependencyId)
                .collect(Collectors.toSet());
    }
}
