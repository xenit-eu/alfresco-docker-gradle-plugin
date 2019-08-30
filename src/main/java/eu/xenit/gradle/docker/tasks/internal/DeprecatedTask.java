package eu.xenit.gradle.docker.tasks.internal;

import eu.xenit.gradle.docker.internal.Deprecation;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskAction;

/**
 * @deprecated since 4.0.0, will be removed in 5.0.0
 */
@Deprecated
public class DeprecatedTask extends DefaultTask {

    private Task replacementTask;

    public void setReplacementTask(Task replacementTask) {
        this.replacementTask = replacementTask;
        dependsOn(replacementTask);
    }

    @TaskAction
    public void runTask() {
        Deprecation.warnDeprecation("The task " + getName()
                + " is deprecated and will be removed in xenit-gradle-plugins 5.0.0. Use "
                + replacementTask.getName() + " instead.");
    }

}
