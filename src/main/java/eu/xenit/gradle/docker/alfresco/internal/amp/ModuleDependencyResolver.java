package eu.xenit.gradle.docker.alfresco.internal.amp;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class ModuleDependencyResolver {

    private final Map<String, ModuleInformation> modules;
    private final Map<String, ModuleWithDependencies> dependencyCache;

    private static Map<String, ModuleInformation> createModuleIdMap(Set<ModuleInformation> dependencies) {
        Map<String, ModuleInformation> moduleIds = new HashMap<>(dependencies.size());
        for (ModuleInformation dependency : dependencies) {
            for (String alias : dependency.getIds()) {
                ModuleInformation previousDependency = moduleIds.put(alias, dependency);
                if(previousDependency != null) {
                    throw new DuplicateModuleException(dependency, previousDependency);
                }
            }
        }
        return moduleIds;
    }

    public ModuleDependencyResolver(Set<ModuleInformation> modules) {
        this.modules = createModuleIdMap(modules);
        dependencyCache = new HashMap<>(modules.size());
    }

    private ModuleWithDependencies resolve(ModuleInformation thisModule, Set<ModuleInformation> visitedModules) {
        if (!dependencyCache.containsKey(thisModule.getId())) {
            if (visitedModules.contains(thisModule)) {
                throw new CircularModuleDependencyException(visitedModules);
            }
            Set<ModuleInformation> newVisitedModules = new HashSet<>(visitedModules);
            newVisitedModules.add(thisModule);
            Set<ModuleWithDependencies> dependencies = new HashSet<>();
            for (String dependencyId : thisModule.getDependencyModuleIds()) {
                ModuleInformation dependencyInformation = modules.get(dependencyId);
                if (dependencyInformation == null) {
                    throw new MissingModuleDependencyException(thisModule, dependencyId);
                }
                dependencies.add(resolve(dependencyInformation, newVisitedModules));
            }
            dependencyCache.put(thisModule.getId(), new ModuleWithDependencies(thisModule, dependencies));
        }
        return dependencyCache.get(thisModule.getId());
    }

    public ModuleWithDependencies resolve(ModuleInformation thisModule) {
        return resolve(thisModule, Collections.emptySet());
    }
}
