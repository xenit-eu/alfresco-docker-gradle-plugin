package eu.xenit.gradle.docker.alfresco.internal.amp;

import de.schlichtherle.truezip.file.TFile;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.alfresco.repo.module.tool.LogOutput;
import org.alfresco.repo.module.tool.WarHelper;
import org.alfresco.repo.module.tool.WarHelperImpl;
import org.alfresco.service.cmr.module.ModuleInstallState;

public final class ModuleDependencySorter {

    private ModuleDependencySorter() {
    }

    static Set<ModuleInformation> readInstalledModulesFromWar(File war) {
        WarHelper warHelper = new WarHelperImpl(o -> {
            // We do not want to log any output from the WarHelper
        });

        return warHelper.listModules(new TFile(war))
                .stream()
                .filter(moduleDetails -> moduleDetails.getInstallState() == ModuleInstallState.INSTALLED)
                .map(ModuleInformationWar::new)
                .collect(Collectors.toSet());
    }

    static List<ModuleWithDependencies> sortByDependencies(Set<ModuleWithDependencies> modules) {
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

    public static List<File> sortByInstallOrder(Set<File> modules, File warFile) {
        Set<ModuleInformation> moduleDependencies = modules.stream()
                .map(ModuleInformationAmp::new)
                .collect(Collectors.toSet());
        moduleDependencies.addAll(readInstalledModulesFromWar(warFile));
        ModuleDependencyResolver moduleDependencyResolver = new ModuleDependencyResolver(moduleDependencies);
        Set<ModuleWithDependencies> moduleWithDependencies = moduleDependencies.stream()
                .map(moduleDependencyResolver::resolve)
                .collect(Collectors.toSet());
        List<ModuleWithDependencies> installOrder = sortByDependencies(moduleWithDependencies);

        return installOrder.stream()
                .filter(m -> m.getModuleInformation() instanceof ModuleInformationAmp)
                .map(m -> m.getModuleInformation().getFile())
                .collect(Collectors.toList());
    }
}
