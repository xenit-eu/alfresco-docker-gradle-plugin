package eu.xenit.gradle.docker.alfresco.tasks.extension;

import eu.xenit.gradle.docker.alfresco.tasks.LabelSupplierTask;
import java.util.Map;
import java.util.function.Supplier;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

public interface LabelConsumerExtension {

    void withLabels(Provider<Map<String, String>> labels);

    void withLabels(Supplier<Map<String, String>> labels);

    void withLabels(Map<String, String> labels);

    void withLabels(LabelSupplierTask task);

    default void withLabels(TaskProvider<? extends LabelSupplierTask> task) {
        withLabels(task.flatMap(LabelSupplierTask::getLabels));
    }

    static LabelConsumerExtension get(Task task) {
        if (task instanceof LabelConsumerExtension) {
            return (LabelConsumerExtension) task;
        } else {
            return task.getConvention().getPlugin(LabelConsumerExtension.class);
        }
    }
}
