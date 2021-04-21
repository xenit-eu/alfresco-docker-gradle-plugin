package eu.xenit.gradle.docker.alfresco.tasks;

import eu.xenit.gradle.docker.alfresco.tasks.extension.LabelConsumerExtension;
import java.util.Map;
import java.util.function.Supplier;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

public interface LabelConsumerTask extends Task, LabelConsumerExtension {

    void withLabels(Provider<Map<String, String>> labels);

    default void withLabels(Supplier<Map<String, String>> labels) {
        withLabels(getProject().provider(labels::get));
    }

    default void withLabels(Map<String, String> labels) {
        withLabels(getProject().provider(() -> labels));
    }

    default void withLabels(LabelSupplierTask task) {
        dependsOn(task);
        withLabels(task.getLabels());
    }

    @Override
    default void withLabels(TaskProvider<? extends LabelSupplierTask> taskProvider) {
        dependsOn(taskProvider);
        withLabels(taskProvider.flatMap(LabelSupplierTask::getLabels));
    }
}
