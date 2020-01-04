package eu.xenit.gradle.tasks;

import org.gradle.api.Task;

import java.util.Map;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.Internal;

public interface LabelSupplierTask extends Task{
    @Internal
    MapProperty<String, String> getLabels();
}
