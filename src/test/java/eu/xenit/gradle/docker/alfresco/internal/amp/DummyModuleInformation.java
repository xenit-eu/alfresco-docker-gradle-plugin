package eu.xenit.gradle.docker.alfresco.internal.amp;

import java.io.File;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

class DummyModuleInformation implements ModuleInformation {

    private final String moduleId;
    private final Set<String> moduleDependencies;
    private final String version;

    public DummyModuleInformation(String moduleId, Set<String> moduleDependencies) {
        this(moduleId, "0.0.0", moduleDependencies);
    }

    public DummyModuleInformation(String moduleId, String version, Set<String> moduleDependencies) {
        this.moduleId = moduleId;
        this.version = version;
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
    public String getVersion() {
        return version;
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
    public String toString() {
        return "DummyModuleInformation{" +
                "moduleId='" + moduleId + '\'' +
                "version='" + version+ '\'' +
                '}';
    }
}
