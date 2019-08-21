package eu.xenit.gradle.alfresco.amp;

import java.io.File;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

class DummyModuleInformation implements ModuleInformation {

    private final String moduleId;
    private final Set<String> moduleDependencies;

    public DummyModuleInformation(String moduleId, Set<String> moduleDependencies) {
        this.moduleId = moduleId;
        this.moduleDependencies = Collections.unmodifiableSet(moduleDependencies);
    }

    @Override
    public File getFile() {
        return null;
    }

    @Override
    public String getId() {
        return moduleId;
    }

    @Override
    public Set<String> getIds() {
        return Collections.singleton(moduleId);
    }

    @Override
    public Set<String> getDependencyModuleIds() {
        return moduleDependencies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DummyModuleInformation that = (DummyModuleInformation) o;
        return moduleId.equals(that.moduleId) &&
                moduleDependencies.equals(that.moduleDependencies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(moduleId, moduleDependencies);
    }

    @Override
    public String toString() {
        return "DummyModuleInformation{" +
                "moduleId='" + moduleId + '\'' +
                '}';
    }
}
