package eu.xenit.gradle.tasks;

import java.util.Map;
import java.util.function.Supplier;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;

public interface LabelConsumerTask extends Task {
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
}
