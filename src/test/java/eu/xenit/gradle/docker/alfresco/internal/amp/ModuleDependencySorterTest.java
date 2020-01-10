package eu.xenit.gradle.docker.alfresco.internal.amp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;

public class ModuleDependencySorterTest {

    private static ModuleWithDependencies createModuleWithDependencies(String name,
            Set<ModuleWithDependencies> dependencies) {
        ModuleInformation moduleInformation = new DummyModuleInformation(name,
                dependencies.stream().map(d -> d.getModuleInformation().getId()).collect(
                        Collectors.toSet()));
        return new ModuleWithDependencies(moduleInformation, dependencies);
    }

    @Test
    public void sortWithoutDependencies() {
        Set<ModuleWithDependencies> modules = new HashSet<>();
        ModuleWithDependencies moduleA = createModuleWithDependencies("module.a", Collections.emptySet());
        modules.add(moduleA);
        ModuleWithDependencies moduleB = createModuleWithDependencies("module.b", Collections.emptySet());
        modules.add(moduleB);
        ModuleWithDependencies moduleC = createModuleWithDependencies("module.c", Collections.emptySet());
        modules.add(moduleC);

        List<ModuleWithDependencies> sortedModules = ModuleDependencySorter.sortByDependencies(modules);

        assertTrue("Sorted by dependencies contains module.a", sortedModules.contains(moduleA));
        assertTrue("Sorted by dependencies contains module.b", sortedModules.contains(moduleB));
        assertTrue("Sorted by dependencies contains module.c", sortedModules.contains(moduleC));
    }

    @Test
    public void sortWithSingleDependencies() {
        Set<ModuleWithDependencies> modules = new HashSet<>();
        ModuleWithDependencies moduleA = createModuleWithDependencies("module.a", Collections.emptySet());
        modules.add(moduleA);
        ModuleWithDependencies moduleB = createModuleWithDependencies("module.b", Collections.singleton(moduleA));
        modules.add(moduleB);
        ModuleWithDependencies moduleC = createModuleWithDependencies("module.c", Collections.singleton(moduleB));
        modules.add(moduleC);

        List<ModuleWithDependencies> sortedModules = ModuleDependencySorter.sortByDependencies(modules);

        assertEquals(Arrays.asList(moduleA, moduleB, moduleC), sortedModules);
    }

    @Test
    public void sortWithMultipleDependencies() {
        Set<ModuleWithDependencies> modules = new HashSet<>();
        ModuleWithDependencies moduleA = createModuleWithDependencies("module.a", Collections.emptySet());
        modules.add(moduleA);
        ModuleWithDependencies moduleB = createModuleWithDependencies("module.b", Collections.emptySet());
        modules.add(moduleB);
        ModuleWithDependencies moduleC = createModuleWithDependencies("module.c",
                new HashSet<>(Arrays.asList(moduleA, moduleB)));
        modules.add(moduleC);

        List<ModuleWithDependencies> sortedModules = ModuleDependencySorter.sortByDependencies(modules);

        assertTrue("module.a is in the sorted modules", sortedModules.contains(moduleA));
        assertTrue("module.c is in the sorted modules", sortedModules.contains(moduleB));
        assertTrue("module.c is in the sorted modules", sortedModules.contains(moduleC));

        int moduleAIndex = sortedModules.indexOf(moduleA);
        int moduleBIndex = sortedModules.indexOf(moduleB);
        int moduleCIndex = sortedModules.indexOf(moduleC);
        assertTrue("module.a is installed before module.c", moduleAIndex < moduleCIndex);
        assertTrue("module.b is installed before module.c", moduleBIndex < moduleCIndex);
    }

}
