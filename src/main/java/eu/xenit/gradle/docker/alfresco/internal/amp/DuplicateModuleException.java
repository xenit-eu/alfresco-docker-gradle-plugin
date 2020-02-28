package eu.xenit.gradle.docker.alfresco.internal.amp;

public class DuplicateModuleException extends ModuleException {

    private final ModuleInformation moduleA;
    private final ModuleInformation moduleB;

    DuplicateModuleException(String message, ModuleInformation moduleA, ModuleInformation moduleB) {
        super(message);
        this.moduleA = moduleA;
        this.moduleB = moduleB;
    }

    public DuplicateModuleException(ModuleInformation moduleA, ModuleInformation moduleB) {
        this("The module "+ createModuleName(moduleA) + " already exists as " + createModuleName(moduleB), moduleA, moduleB);
    }

    private static String createModuleName(ModuleInformation moduleInformation) {
        return moduleInformation.getId() + " version " + moduleInformation.getVersion();
    }

    public ModuleInformation getModuleB() {
        return moduleB;
    }

    public ModuleInformation getModuleA() {
        return moduleA;
    }
}
