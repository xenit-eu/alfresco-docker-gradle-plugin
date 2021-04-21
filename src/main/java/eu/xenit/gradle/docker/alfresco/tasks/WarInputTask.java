package eu.xenit.gradle.docker.alfresco.tasks;

import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskProvider;

public interface WarInputTask extends Task {

    @InputFile
    @SkipWhenEmpty
    RegularFileProperty getInputWar();

    default void setInputWar(FileCollection configuration) {
        dependsOn(configuration);
        getInputWar().set(getProject().getLayout().file(getProject().provider(() -> {
            if (configuration.isEmpty()) {
                return null;
            }
            return configuration.getSingleFile();
        })));
    }

    default void setInputWar(WarOutputTask task) {
        getInputWar().set(task.getOutputWar());
    }

    default void setInputWar(TaskProvider<? extends WarOutputTask> taskProvider) {
        dependsOn(taskProvider);
        getInputWar().set(taskProvider.flatMap(WarOutputTask::getOutputWar));
    }

}
