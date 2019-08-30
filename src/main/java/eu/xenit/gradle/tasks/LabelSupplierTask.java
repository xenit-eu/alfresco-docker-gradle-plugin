package eu.xenit.gradle.tasks;

import org.gradle.api.Task;

import java.util.Map;
import org.gradle.api.tasks.Internal;

public interface LabelSupplierTask extends Task{
    @Internal
    Map<String, String> getLabels();
}
