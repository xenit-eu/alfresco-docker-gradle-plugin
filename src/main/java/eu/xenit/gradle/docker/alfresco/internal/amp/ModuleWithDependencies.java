package eu.xenit.gradle.docker.alfresco.internal.amp;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

class ModuleWithDependencies {

    private final ModuleInformation moduleInformation;
    private final Set<ModuleWithDependencies> dependencies;

    public ModuleWithDependencies(ModuleInformation moduleInformation,
            Set<ModuleWithDependencies> dependencies) {
        this.moduleInformation = moduleInformation;
        this.dependencies = Collections.unmodifiableSet(dependencies);
    }

    public Set<ModuleWithDependencies> getDependencies() {
        return dependencies;
    }

    public ModuleInformation getModuleInformation() {
        return moduleInformation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ModuleWithDependencies that = (ModuleWithDependencies) o;
        return getModuleInformation().equals(that.getModuleInformation()) &&
                getDependencies().equals(that.getDependencies());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getModuleInformation(), getDependencies());
    }
}
