package eu.xenit.gradle.alfresco.amp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ModuleDependencySorter {

    static List<ModuleWithDependencies> sortByDependencies(Set<ModuleWithDependencies> modules) {
        List<ModuleWithDependencies> orderedDependencyList = new ArrayList<>();
        for (ModuleWithDependencies module : modules) {
            if(!orderedDependencyList.contains(module)) {
                List<ModuleWithDependencies> moduleDependencies = sortByDependencies(module.getDependencies());
                for (ModuleWithDependencies moduleDependency : moduleDependencies) {
                    if(!orderedDependencyList.contains(moduleDependency)) {
                        orderedDependencyList.add(moduleDependency);
                    }
                }
                orderedDependencyList.add(module);
            }
        }
        return orderedDependencyList;
    }

    public static List<File> sortByInstallOrder(Set<File> modules) {
        Set<ModuleInformation> moduleDependencies = modules.stream()
                .map(ModuleInformationImpl::new)
                .collect(Collectors.toSet());
        ModuleDependencyResolver moduleDependencyResolver = new ModuleDependencyResolver(moduleDependencies);
        Set<ModuleWithDependencies> moduleWithDependencies = moduleDependencies.stream()
                .map(moduleDependencyResolver::resolve)
                .collect(Collectors.toSet());
        List<ModuleWithDependencies> installOrder = sortByDependencies(moduleWithDependencies);

        return installOrder.stream().map(m -> m.getModuleInformation().getFile()).collect(Collectors.toList());
    }
}
