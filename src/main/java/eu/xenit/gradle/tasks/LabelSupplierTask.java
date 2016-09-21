package eu.xenit.gradle.tasks;

import org.gradle.api.Task;

import java.util.Map;

public interface LabelSupplierTask extends Task{
    Map<String, String> getLabels();
}
