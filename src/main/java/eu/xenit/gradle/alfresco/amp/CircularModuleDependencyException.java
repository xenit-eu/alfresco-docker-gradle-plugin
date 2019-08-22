package eu.xenit.gradle.alfresco.amp;

import java.util.Set;

public class CircularModuleDependencyException extends RuntimeException {

    private final Set<ModuleInformation> dependencyLoop;

    public CircularModuleDependencyException(Set<ModuleInformation> modules) {
        super("A circular dependency was detected between modules: " + createLoopMessage(modules));
        this.dependencyLoop = modules;
    }

    private static String createLoopMessage(Set<ModuleInformation> modules) {
        if(modules.isEmpty()) {
            return "<unknown>";
        }
        StringBuilder stringBuilder = new StringBuilder();
        ModuleInformation firstModule = null;
        for (ModuleInformation module : modules) {
            if (firstModule == null) {
                firstModule = module;
            }
            stringBuilder.append(module.getId()).append(" -> ");
        }
        return stringBuilder.append(firstModule.getId()).toString();
    }

    public Set<ModuleInformation> getDependencyLoop() {
        return dependencyLoop;
    }
}
