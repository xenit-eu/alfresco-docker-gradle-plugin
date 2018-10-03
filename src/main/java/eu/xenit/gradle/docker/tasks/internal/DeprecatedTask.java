package eu.xenit.gradle.docker.tasks.internal;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

public class DeprecatedTask extends DefaultTask {
    private Task replacementTask;

    public void setReplacementTask(Task replacementTask) {
        this.replacementTask = replacementTask;
        dependsOn(replacementTask);
    }

    @TaskAction
    public void runTask() {
        getLogger()
                .warn("[eu.xenit.docker] The task " + getName()
                        + " is deprecated and will be removed in xenit-gradle-plugins 5.0.0. Use "
                        + replacementTask.getName() + " instead.");
    }

}
