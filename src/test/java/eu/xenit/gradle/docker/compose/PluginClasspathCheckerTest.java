package eu.xenit.gradle.docker.compose;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertTrue;

import eu.xenit.gradle.docker.DockerPlugin;
import eu.xenit.gradle.docker.alfresco.tasks.MergeWarsTask;
import eu.xenit.gradle.docker.compose.PluginClasspathChecker.PluginClasspathPollutionException;
import eu.xenit.gradle.docker.tasks.internal.DockerBuildImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.function.Supplier;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PluginClasspathCheckerTest {


    private static class CustomClassLoader extends ClassLoader {

        private ClassLoader currentLoader;

        public CustomClassLoader(ClassLoader parent, ClassLoader currentLoader) {
            super(parent);
            this.currentLoader = currentLoader;
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            if (!name.startsWith("eu.xenit.gradle.docker.")) {
                return currentLoader.loadClass(name);
            }
            byte[] bt = loadClassData(name);
            return defineClass(name, bt, 0, bt.length);
        }

        @Override
        protected URL findResource(String s) {
            return currentLoader.getResource(s);
        }

        @Override
        protected Enumeration<URL> findResources(String s) throws IOException {
            return currentLoader.getResources(s);
        }

        private byte[] loadClassData(String className) throws ClassNotFoundException {
            //read class
            InputStream is = currentLoader.getResourceAsStream(className.replace(".", "/") + ".class");
            ByteArrayOutputStream byteSt = new ByteArrayOutputStream();
            //write into byte
            int len = 0;
            try {
                while ((len = is.read()) != -1) {
                    byteSt.write(len);
                }
            } catch (IOException e) {
                throw new ClassNotFoundException(className);
            }
            //convert into byte array
            return byteSt.toByteArray();
        }

    }

    private ClassLoader classLoaderA;
    private ClassLoader classLoaderB;

    private Class<Plugin<Project>> dockerPluginA;
    private Class<Plugin<Project>> dockerPluginB;

    private ProjectInternal projectA;
    private ProjectInternal projectB;

    private PluginClasspathChecker classpathChecker;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setupClassLoader() throws ClassNotFoundException {
        ClassLoader parentClassLoader = DockerPlugin.class.getClassLoader().getParent();
        classLoaderA = new CustomClassLoader(parentClassLoader, Project.class.getClassLoader());
        classLoaderB = new CustomClassLoader(parentClassLoader, Project.class.getClassLoader());

        dockerPluginA = loadClassWithClassloader(classLoaderA, DockerPlugin.class);
        dockerPluginB = loadClassWithClassloader(classLoaderB, DockerPlugin.class);

        projectA = (ProjectInternal) ProjectBuilder.builder().withName("project-a").build();
        projectB = (ProjectInternal) ProjectBuilder.builder().withName("project-b").build();
        classpathChecker = new PluginClasspathChecker(projectA);
    }

    @SuppressWarnings("unchecked")
    private <T, U extends T> Class<T> loadClassWithClassloader(ClassLoader loader, Class<U> type)
            throws ClassNotFoundException {
        return (Class<T>) loader.loadClass(type.getName());
    }

    private <T> T withClassloader(ClassLoader classloader, Supplier<T> producer) {
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classloader);
            return producer.get();
        } finally {
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
    }

    @Test
    public void testCheckPluginSameClasspath() {
        withClassloader(classLoaderA, () -> {
            projectB.getPlugins().apply(dockerPluginA);
            return null;
        });

        projectA.evaluate();
        projectB.evaluate();

        assertTrue(projectB.getPlugins().hasPlugin(DockerPlugin.PLUGIN_ID));

        classpathChecker.checkPlugin(projectB, dockerPluginA, DockerPlugin.PLUGIN_ID);
    }

    @Test
    public void testCheckPluginDifferentClasspath() {
        withClassloader(classLoaderB, () -> {
            projectB.getPlugins().apply(dockerPluginB);
            return null;
        });

        projectA.evaluate();
        projectB.evaluate();

        assertTrue(projectB.getPlugins().hasPlugin(DockerPlugin.PLUGIN_ID));

        expectedException.expect(PluginClasspathPollutionException.class);
        expectedException.expectMessage(new PluginClasspathPollutionException(projectA, projectB, DockerPlugin.PLUGIN_ID).getMessage());

        classpathChecker.checkPlugin(projectB, dockerPluginA, DockerPlugin.PLUGIN_ID);
    }

    @Test
    public void testCheckTaskSameClasspath() throws ClassNotFoundException {
        Class<Task> taskA = loadClassWithClassloader(classLoaderA, DockerBuildImage.class);

        projectA.getTasks().register("dockerBuildImage", taskA);
        projectB.getTasks().register("dockerBuildImage", taskA);

        projectA.evaluate();
        projectB.evaluate();

        classpathChecker.checkTask(taskA, projectA.getTasks().getByName("dockerBuildImage"));
        classpathChecker.checkTask(taskA, projectB.getTasks().getByName("dockerBuildImage"));
    }

    @Test
    public void testCheckTaskSameClasspathDifferentTaskType() throws ClassNotFoundException {
        Class<Task> taskA = loadClassWithClassloader(classLoaderA, DockerBuildImage.class);
        Class<Task> taskAOther = loadClassWithClassloader(classLoaderA, MergeWarsTask.class);

        projectA.getTasks().register("dockerBuildImage", taskA);
        projectA.getTasks().register("mergeWarsTask", taskAOther);

        projectB.getTasks().register("dockerBuildImage", taskA);
        projectB.getTasks().register("mergeWarsTask", taskAOther);

        projectA.evaluate();
        projectB.evaluate();

        expectedException.expect(ClassCastException.class);
        expectedException.expect(not(instanceOf(PluginClasspathPollutionException.class)));

        classpathChecker.checkTask(taskA, projectB.getTasks().getByName("mergeWarsTask"));
    }

    @Test
    public void testCheckTaskDifferentClasspath() throws ClassNotFoundException {
        Class<Task> taskA = loadClassWithClassloader(classLoaderA, DockerBuildImage.class);
        Class<Task> taskB = loadClassWithClassloader(classLoaderB, DockerBuildImage.class);

        projectA.getTasks().register("dockerBuildImage", taskA);
        projectB.getTasks().register("dockerBuildImage", taskB);

        projectA.evaluate();
        projectB.evaluate();

        expectedException.expect(PluginClasspathPollutionException.class);
        expectedException.expectMessage(new PluginClasspathPollutionException(projectA, projectB, projectB.getTasks().getByName("dockerBuildImage")).getMessage());

        classpathChecker.checkTask(taskA, projectB.getTasks().getByName("dockerBuildImage"));
    }

    @Test
    public void testCheckTaskDifferentClasspathDifferentTaskType() throws ClassNotFoundException {
        Class<Task> taskA = loadClassWithClassloader(classLoaderA, DockerBuildImage.class);
        Class<Task> taskAOther = loadClassWithClassloader(classLoaderA, MergeWarsTask.class);
        Class<Task> taskBOther = loadClassWithClassloader(classLoaderB, MergeWarsTask.class);

        projectA.getTasks().register("mergeWarsTask", taskAOther);

        projectB.getTasks().register("mergeWarsTask", taskBOther);

        projectA.evaluate();
        projectB.evaluate();

        expectedException.expect(ClassCastException.class);
        expectedException.expect(not(instanceOf(PluginClasspathPollutionException.class)));

        classpathChecker.checkTask(taskA, projectB.getTasks().getByName("mergeWarsTask"));
    }

}
