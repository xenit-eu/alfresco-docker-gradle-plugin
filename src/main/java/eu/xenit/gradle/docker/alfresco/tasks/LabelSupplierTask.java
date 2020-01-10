package eu.xenit.gradle.docker.alfresco.tasks;

import org.gradle.api.Task;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.Internal;

public interface LabelSupplierTask extends Task {

    @Internal
    MapProperty<String, String> getLabels();
}
