package eu.xenit.gradle.docker.compose;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

/**
 * Validates that plugins of an other project are on the same (or parent) classpath of where this plugin is running.
 * <p>
 * Gradle isolates classpaths of the `plugins {}` block between sibling projects, but for the cross-project functionality,
 * we need some plugin and task classes to be identical between projects (so not loaded from different classloaders).
 * <p>
 * Not having the same class identity will result in either {@link ClassCastException}, or some tasks that are seemingly ignored.
 * Receiving such exceptions would be very confusing to the user, so when this case is detected, we pre-emptively throw an exception with a custom message.
 */
public class PluginClasspathChecker {

    private static final String KILL_SWITCH = "eu.xenit.gradle.docker.flags.PluginClasspathChecker.v1.disabled";

    private final Project project;

    public static class PluginClasspathPollutionException extends ClassCastException {

        private static String createMessage(Project sourceProject, Project targetProject, String identifierType,
                String identifier) {
            return String
                    .format("The '%s' %s has a different class identity in %s than in %s.", identifier, identifierType,
                            targetProject.toString(), sourceProject.toString());
        }

        public PluginClasspathPollutionException(Project sourceProject, Project targetProject, String pluginId) {
            super(createMessage(sourceProject, targetProject, "plugin", pluginId)
                    + String.format("\nDefine the %s plugin once in your root project to fix this problem.", pluginId));
        }

        public PluginClasspathPollutionException(Project sourceProject, Project targetProject, Task task) {
            super(createMessage(sourceProject, targetProject, "task", task.getPath())
                    + "\nDefine the plugin that created this task once in your root project to fix this problem.");
        }
    }

    private static boolean isDisabled() {
        return Boolean.getBoolean(System.getProperty(KILL_SWITCH, "false"));
    }

    public PluginClasspathChecker(Project project) {
        this.project = project;
    }

    /**
     * Checks that a plugin {@code pluginId} in the {@code targetProject} has the same class identity as the {@code plugin} class
     * <p>
     * This check will only be run when a plugin {@code pluginId} is applied to the project.
     *
     * @param targetProject Project to check for the plugin
     * @param plugin        Plugin class (or parent class of the plugin) that must be the class of the {@code pluginId} plugin
     * @param pluginId      Plugin id that will be checked for consistency (at the time the plugin is applied)
     */
    public void checkPlugin(Project targetProject, Class<? extends Plugin<Project>> plugin, String pluginId) {
        if(isDisabled())
            return;
        withPlugin(targetProject, plugin, pluginId, appliedPlugin -> {
            // Empty
        });
    }

    /**
     * Checks that a plugin {@code pluginId} in the {@code targetProject} has the same class identity as the {@code plugin} class,
     * and then runs an action with that plugin.
     * <p>
     * This check and action will only be run when a plugin {@code pluginId} is applied to the project.
     *
     * @param targetProject Project to check for the plugin
     * @param plugin        Plugin class (or parent class of the plugin) that must be the class of the {@code pluginId} plugin
     * @param pluginId      Plugin id that will be checked for consistency (at the time the plugin is applied)
     * @param action        Action to run on the plugin {@code plugin}
     * @param <T>           The specific type of the plugin
     */
    public <T extends Plugin<Project>> void withPlugin(Project targetProject, Class<T> plugin, String pluginId,
            Action<? super T> action) {
        if(isDisabled()) {
            targetProject.getPlugins().withType(plugin, action);
        }
        targetProject.getPlugins().withId(pluginId, appliedPlugin -> {
            if (!plugin.isAssignableFrom(appliedPlugin.getClass())) {
                throw new PluginClasspathPollutionException(project, targetProject, pluginId);
            }
        });
        targetProject.getPlugins().withType(plugin, appliedPlugin -> {
            // Plugins applied by class first trigger the withType(), before the plugin id itself is registered.
            // Try to find the plugin with hasPlugin(), and if it does not exist, try to apply it again.
            // If a plugin has already been applied, it will return the same instance. If not, we have a classpath problem here
            if (!targetProject.getPlugins().hasPlugin(pluginId)
                    && targetProject.getPlugins().apply(pluginId) != appliedPlugin) {
                throw new PluginClasspathPollutionException(project, targetProject, pluginId);
            }
            action.execute(appliedPlugin);
        });
    }

    /**
     * Checks that a task {@code taskInstance} has the same class identity as {@code taskClass}
     * <p>
     * In the case that a task class with the same name ({@link Class#getName()} is detected, but with a different identity,
     * the {@link PluginClasspathPollutionException} is thrown to indicate a problem with the plugin classpath.
     *
     * @param taskClass    Task class (or parent class of the task) that must be the type of the {@code taskInstance} instance
     * @param taskInstance Task instance that will be checked to be of type {@code taskClass}
     * @param <T>          The specific type of the class
     * @return Task instance that has safely been casted to {@code taskClass}
     */
    public <T extends Task> T checkTask(Class<T> taskClass, Task taskInstance) {
        if (isDisabled() || taskClass.isAssignableFrom(taskInstance.getClass())) {
            return (T) taskInstance;
        }

        Class<?> taskInstanceClass = taskInstance.getClass();

        while (!taskClass.getName().equals(taskInstanceClass.getName()) && !taskInstanceClass
                .isAssignableFrom(taskClass)) {
            taskInstanceClass = taskInstanceClass.getSuperclass();
        }
        // The names equal, but the identities don't OR we went so far that we got to a class that is the parent of our taskClass (some gradle base class or Object)

        // If the names are equal now, so this is plugin classpath pollution
        if (taskClass.getName().equals(taskInstanceClass.getName())) {
            throw new PluginClasspathPollutionException(project, taskInstance.getProject(), taskInstance);
        }

        // It really is a totally different class, just try a cast so we generate a ClassCastException
        throw new ClassCastException(String.format("%s cannot be cast to %s", taskInstance.getClass(), taskClass));
    }
}
