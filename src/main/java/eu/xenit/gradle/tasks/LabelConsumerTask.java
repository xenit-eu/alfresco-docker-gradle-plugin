package eu.xenit.gradle.tasks;

import org.gradle.api.Task;

import java.util.Map;
import java.util.function.Supplier;

public interface LabelConsumerTask extends Task {
    void withLabels(Supplier<Map<String, String>> labels);

    default void withLabels(Map<String, String> labels) {
        withLabels((Supplier<Map<String, String>>) ()->labels);
    }

    default void withLabels(LabelSupplierTask task) {
        dependsOn(task);
        withLabels((Supplier<Map<String, String>>) task::getLabels);
    }
}
