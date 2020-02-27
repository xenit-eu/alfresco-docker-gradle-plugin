package eu.xenit.gradle.docker.alfresco.internal.amp;

import de.schlichtherle.truezip.file.TFile;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.alfresco.repo.module.tool.WarHelper;
import org.alfresco.repo.module.tool.WarHelperImpl;
import org.alfresco.service.cmr.module.ModuleInstallState;

public final class ModuleDependencySorter {

    private ModuleDependencySorter() {
    }

    private static Set<ModuleInformation> readInstalledModulesFromWar(File war) {
        WarHelper warHelper = new WarHelperImpl(o -> {
            // We do not want to log any output from the WarHelper
        });

        return warHelper.listModules(new TFile(war))
                .stream()
                .filter(moduleDetails -> moduleDetails.getInstallState() == ModuleInstallState.INSTALLED)
                .map(ModuleInformationWar::new)
                .collect(Collectors.toSet());
    }

    private static List<ModuleWithDependencies> sortByDependencies(Set<ModuleWithDependencies> modules) {
        List<ModuleWithDependencies> orderedDependencyList = new ArrayList<>();
        for (ModuleWithDependencies module : modules) {
            if (!orderedDependencyList.contains(module)) {
                List<ModuleWithDependencies> moduleDependencies = sortByDependencies(module.getDependencies());
                for (ModuleWithDependencies moduleDependency : moduleDependencies) {
                    if (!orderedDependencyList.contains(moduleDependency)) {
                        orderedDependencyList.add(moduleDependency);
                    }
                }
                orderedDependencyList.add(module);
            }
        }
        return orderedDependencyList;
    }

    static List<ModuleInformation> sortByInstallOrder(Set<ModuleInformation> modules,
            Set<ModuleInformation> warModules) {
        Set<ModuleInformation> moduleDependencies = new HashSet<>(modules);
        moduleDependencies.addAll(warModules);

        ModuleDependencyResolver moduleDependencyResolver = new ModuleDependencyResolver(moduleDependencies);
        Set<ModuleWithDependencies> moduleWithDependencies = modules.stream()
                .map(moduleDependencyResolver::resolve)
                .collect(Collectors.toSet());
        List<ModuleWithDependencies> installOrder = sortByDependencies(moduleWithDependencies);
        return installOrder.stream()
                .filter(m -> modules.contains(m.getModuleInformation()))
                .map(ModuleWithDependencies::getModuleInformation)
                .collect(Collectors.toList());
    }

    public static List<ModuleInformation> sortByInstallOrder(Set<File> modules, File warFile) {
        Set<ModuleInformation> informationFromModules = modules.stream()
                .map(ModuleInformationAmp::new)
                .collect(Collectors.toSet());
        Set<ModuleInformation> informationFromWar = readInstalledModulesFromWar(warFile);
        return sortByInstallOrder(informationFromModules, informationFromWar);
    }
}
