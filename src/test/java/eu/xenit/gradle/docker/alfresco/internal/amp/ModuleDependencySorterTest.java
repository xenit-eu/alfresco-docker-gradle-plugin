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
        Set<ModuleInformation> modules = new HashSet<>();
        modules.add(new DummyModuleInformation("module.a", Collections.emptySet()));
        modules.add(new DummyModuleInformation("module.b", Collections.emptySet()));
        modules.add(new DummyModuleInformation("module.c", Collections.emptySet()));

        List<String> sortedModules = ModuleDependencySorter.sortByInstallOrder(modules, Collections.emptySet()).stream()
                .map(ModuleInformation::getId)
                .collect(Collectors.toList());

        assertTrue("Sorted by dependencies contains module.a", sortedModules.contains("module.a"));
        assertTrue("Sorted by dependencies contains module.b", sortedModules.contains("module.b"));
        assertTrue("Sorted by dependencies contains module.c", sortedModules.contains("module.c"));
    }

    @Test
    public void sortWithSingleDependencies() {
        Set<ModuleInformation> modules = new HashSet<>();
        modules.add(new DummyModuleInformation("module.a", Collections.emptySet()));
        modules.add(new DummyModuleInformation("module.b", Collections.singleton("module.a")));
        modules.add(new DummyModuleInformation("module.c", Collections.singleton("module.b")));

        List<String> sortedModules = ModuleDependencySorter.sortByInstallOrder(modules, Collections.emptySet()).stream()
                .map(ModuleInformation::getId)
                .collect(Collectors.toList());

        assertEquals(Arrays.asList("module.a", "module.b", "module.c"), sortedModules);
    }

    @Test
    public void sortWithMultipleDependencies() {
        Set<ModuleInformation> modules = new HashSet<>();
        modules.add(new DummyModuleInformation("module.a", Collections.emptySet()));
        modules.add(new DummyModuleInformation("module.b", Collections.emptySet()));
        modules.add(new DummyModuleInformation("module.c", new HashSet<>(Arrays.asList("module.a", "module.b"))));

        List<String> sortedModules = ModuleDependencySorter.sortByInstallOrder(modules, Collections.emptySet()).stream()
                .map(ModuleInformation::getId)
                .collect(Collectors.toList());

        assertTrue("module.a is in the sorted modules", sortedModules.contains("module.a"));
        assertTrue("module.c is in the sorted modules", sortedModules.contains("module.b"));
        assertTrue("module.c is in the sorted modules", sortedModules.contains("module.c"));

        int moduleAIndex = sortedModules.indexOf("module.a");
        int moduleBIndex = sortedModules.indexOf("module.b");
        int moduleCIndex = sortedModules.indexOf("module.c");
        assertTrue("module.a is installed before module.c", moduleAIndex < moduleCIndex);
        assertTrue("module.b is installed before module.c", moduleBIndex < moduleCIndex);
    }

    @Test
    public void sortDoesNotListWarModules() {
        Set<ModuleInformation> amps = new HashSet<>();
        Set<ModuleInformation> warModules = new HashSet<>();

        warModules.add(new DummyModuleInformation("module.a", Collections.emptySet()));
        amps.add(new DummyModuleInformation("module.b", Collections.emptySet()));
        amps.add(new DummyModuleInformation("module.c", new HashSet<>(Arrays.asList("module.a", "module.b"))));

        List<ModuleInformation> sortedInInstallationOrder = ModuleDependencySorter.sortByInstallOrder(amps, warModules);

        List<String> modulesToInstall = sortedInInstallationOrder.stream()
                .map(ModuleInformation::getId)
                .collect(Collectors.toList());

        assertEquals(Arrays.asList("module.b", "module.c"), modulesToInstall);
    }

    @Test(expected = ModuleAlreadyInstalledException.class)
    public void sortRejectsAlreadyInstalledModules() {
        Set<ModuleInformation> amps = new HashSet<>();
        Set<ModuleInformation> warModules = new HashSet<>();

        warModules.add(new DummyModuleInformation("module.a", Collections.emptySet()));
        amps.add(new DummyModuleInformation("module.a", Collections.emptySet()));

        ModuleDependencySorter.sortByInstallOrder(amps, warModules);
    }
}
