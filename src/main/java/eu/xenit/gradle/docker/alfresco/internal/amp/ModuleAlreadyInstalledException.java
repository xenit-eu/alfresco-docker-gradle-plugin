package eu.xenit.gradle.docker.alfresco.internal.amp;

public class ModuleAlreadyInstalledException extends DuplicateModuleException {

    private final ModuleInformation moduleToInstall;
    private final ModuleInformation existingModule;

    public ModuleAlreadyInstalledException(ModuleInformation moduleToInstall, ModuleInformation existingModule) {
        super("The module "+ createModuleName(moduleToInstall) + " is already installed as " + createModuleName(existingModule), moduleToInstall, existingModule);
        this.moduleToInstall = moduleToInstall;
        this.existingModule = existingModule;
    }

    private static String createModuleName(ModuleInformation moduleInformation) {
        return moduleInformation.getId() + " version " + moduleInformation.getVersion();
    }

    public ModuleInformation getExistingModule() {
        return existingModule;
    }

    public ModuleInformation getModuleToInstall() {
        return moduleToInstall;
    }
}
