package eu.xenit.gradle.docker.alfresco.internal.amp;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class ModuleDependencyResolverTest {

    @Test
    public void resolveModuleWithoutDependencies() {
        Set<ModuleInformation> modules = new HashSet<>();
        ModuleInformation moduleA = new DummyModuleInformation("module.a", Collections.emptySet());
        modules.add(moduleA);
        modules.add(new DummyModuleInformation("module.b", Collections.emptySet()));
        modules.add(new DummyModuleInformation("module.c", Collections.emptySet()));

        ModuleDependencyResolver dependencyResolver = new ModuleDependencyResolver(
                Collections.unmodifiableSet(modules));

        ModuleWithDependencies moduleWithDependencies = dependencyResolver.resolve(moduleA);
        assertEquals("module.a is resolved", moduleA, moduleWithDependencies.getModuleInformation());
        assertTrue("module.a has no dependencies", moduleWithDependencies.getDependencies().isEmpty());
    }

    @Test
    public void resolveModuleWithSingleDependency() {
        Set<ModuleInformation> modules = new HashSet<>();
        ModuleInformation moduleA = new DummyModuleInformation("module.a", Collections.emptySet());
        modules.add(moduleA);
        ModuleInformation moduleB = new DummyModuleInformation("module.b", Collections.singleton(moduleA.getId()));
        modules.add(moduleB);
        ModuleInformation moduleC = new DummyModuleInformation("module.c", Collections.singleton(moduleA.getId()));
        modules.add(moduleC);

        ModuleDependencyResolver dependencyResolver = new ModuleDependencyResolver(
                Collections.unmodifiableSet(modules));

        ModuleWithDependencies moduleAWithDependencies = dependencyResolver.resolve(moduleA);
        assertEquals("module.a is resolved", moduleA, moduleAWithDependencies.getModuleInformation());
        assertTrue("module.a has no dependencies", moduleAWithDependencies.getDependencies().isEmpty());

        ModuleWithDependencies moduleBWithDependencies = dependencyResolver.resolve(moduleB);
        assertEquals("module.b is resolved", moduleB, moduleBWithDependencies.getModuleInformation());
        assertEquals("module.b has a dependency on module.a", Collections.singleton(moduleAWithDependencies),
                moduleBWithDependencies.getDependencies());

        ModuleWithDependencies moduleCWithDependencies = dependencyResolver.resolve(moduleC);
        assertEquals("module.c is resolved", moduleC, moduleCWithDependencies.getModuleInformation());
        assertEquals("module.c has a dependency on module.a", Collections.singleton(moduleAWithDependencies),
                moduleCWithDependencies.getDependencies());
    }

    @Test(expected = CircularModuleDependencyException.class)
    public void resolveModuleWithCircularDependency() {
        Set<ModuleInformation> modules = new HashSet<>();
        ModuleInformation moduleA = new DummyModuleInformation("module.a", Collections.singleton("module.c"));
        modules.add(moduleA);
        ModuleInformation moduleB = new DummyModuleInformation("module.b", Collections.singleton(moduleA.getId()));
        modules.add(moduleB);
        ModuleInformation moduleC = new DummyModuleInformation("module.c", Collections.singleton(moduleB.getId()));
        modules.add(moduleC);

        ModuleDependencyResolver dependencyResolver = new ModuleDependencyResolver(
                Collections.unmodifiableSet(modules));

        dependencyResolver.resolve(moduleA);
    }

    @Test(expected = MissingModuleDependencyException.class)
    public void resolveModuleWithMissingDependency() {
        Set<ModuleInformation> modules = new HashSet<>();
        ModuleInformation moduleA = new DummyModuleInformation("module.a", Collections.singleton("module.c"));
        modules.add(moduleA);
        ModuleInformation moduleB = new DummyModuleInformation("module.b", Collections.singleton(moduleA.getId()));
        modules.add(moduleB);

        ModuleDependencyResolver dependencyResolver = new ModuleDependencyResolver(
                Collections.unmodifiableSet(modules));

        dependencyResolver.resolve(moduleB);
    }

    @Test
    public void resolveModuleWithMultipleDependencies() {
        Set<ModuleInformation> modules = new HashSet<>();
        ModuleInformation moduleA = new DummyModuleInformation("module.a", Collections.emptySet());
        modules.add(moduleA);
        ModuleInformation moduleB = new DummyModuleInformation("module.b", Collections.singleton(moduleA.getId()));
        modules.add(moduleB);
        ModuleInformation moduleC = new DummyModuleInformation("module.c", new HashSet<>(
                Arrays.asList(moduleA.getId(), moduleB.getId())));
        modules.add(moduleC);

        ModuleDependencyResolver dependencyResolver = new ModuleDependencyResolver(
                Collections.unmodifiableSet(modules));

        ModuleWithDependencies moduleAWithDependencies = dependencyResolver.resolve(moduleA);
        assertEquals("module.a is resolved", moduleA, moduleAWithDependencies.getModuleInformation());
        assertTrue("module.a has no dependencies", moduleAWithDependencies.getDependencies().isEmpty());

        ModuleWithDependencies moduleBWithDependencies = dependencyResolver.resolve(moduleB);
        assertEquals("module.b is resolved", moduleB, moduleBWithDependencies.getModuleInformation());
        assertEquals("module.b has a dependency on module.a", Collections.singleton(moduleAWithDependencies),
                moduleBWithDependencies.getDependencies());

        ModuleWithDependencies moduleCWithDependencies = dependencyResolver.resolve(moduleC);
        assertEquals("module.c is resolved", moduleC, moduleCWithDependencies.getModuleInformation());
        assertEquals("module.c has a dependency on module.a and module.b",
                new HashSet<>(Arrays.asList(moduleAWithDependencies, moduleBWithDependencies)),
                moduleCWithDependencies.getDependencies());
    }

    @Test(expected = DuplicateModuleException.class)
    public void resolveDuplicateModules() {
        Set<ModuleInformation> modules = new HashSet<>();
        ModuleInformation moduleA = new DummyModuleInformation("module.a", Collections.emptySet());
        modules.add(moduleA);
        ModuleInformation moduleB = new DummyModuleInformation("module.b", Collections.singleton(moduleA.getId()));
        modules.add(moduleB);
        ModuleInformation moduleBbis = new DummyModuleInformation("module.b", Collections.singleton(moduleA.getId()));
        modules.add(moduleBbis);

        new ModuleDependencyResolver(Collections.unmodifiableSet(modules));
    }

}
