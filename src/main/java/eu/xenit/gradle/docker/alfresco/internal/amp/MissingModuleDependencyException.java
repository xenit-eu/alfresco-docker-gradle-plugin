package eu.xenit.gradle.docker.alfresco.internal.amp;

public class MissingModuleDependencyException extends RuntimeException {

    private final ModuleInformation thisModule;
    private final String dependencyId;

    public MissingModuleDependencyException(ModuleInformation thisModule, String dependencyId) {
        super("The module " + thisModule.getId() + " has an unresolved dependency on " + dependencyId);
        this.thisModule = thisModule;
        this.dependencyId = dependencyId;
    }

    public ModuleInformation getThisModule() {
        return thisModule;
    }

    public String getDependencyId() {
        return dependencyId;
    }
}
