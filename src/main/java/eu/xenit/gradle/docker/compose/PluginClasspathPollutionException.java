package eu.xenit.gradle.docker.compose;

import org.gradle.api.Project;
import org.gradle.api.Task;

public class PluginClasspathPollutionException extends ClassCastException {

    private static String createMessage(Project sourceProject, Project targetProject, String identifierType,
            String identifier) {
        return String
                .format("The '%s' %s has a different class identity in %s than in %s.", identifier, identifierType,
                        targetProject.toString(), sourceProject.toString());
    }

    public PluginClasspathPollutionException(String message) {
        super(message);
    }

    PluginClasspathPollutionException(Project sourceProject, Project targetProject, String pluginId) {
        this(createMessage(sourceProject, targetProject, "plugin", pluginId)
                + String.format("\nDefine the %s plugin once in your root project to fix this problem.", pluginId));
    }

    PluginClasspathPollutionException(Project sourceProject, Project targetProject, Task task) {
        this(createMessage(sourceProject, targetProject, "task", task.getPath())
                + "\nDefine the plugin that created this task once in your root project to fix this problem.");
    }
}
