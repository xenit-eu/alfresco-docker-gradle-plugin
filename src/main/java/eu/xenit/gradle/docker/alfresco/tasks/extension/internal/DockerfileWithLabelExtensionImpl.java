package eu.xenit.gradle.docker.alfresco.tasks.extension.internal;

import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import eu.xenit.gradle.docker.alfresco.tasks.LabelSupplierTask;
import eu.xenit.gradle.docker.alfresco.tasks.extension.LabelConsumerExtension;
import java.util.Map;
import java.util.function.Supplier;
import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Provider;

public class DockerfileWithLabelExtensionImpl implements LabelConsumerExtension {

    public static void applyTo(Dockerfile task) {
        DockerfileWithLabelExtensionImpl impl = task.getProject().getObjects()
                .newInstance(DockerfileWithLabelExtensionImpl.class, task);
        task.getConvention().getPlugins().put("labels", impl);
    }

    private final Dockerfile dockerfile;
    private boolean labelActionAdded = false;
    /**
     * Map of labels to add to the dockerfile
     */
    private final MapProperty<String, String> labels;

    @Inject
    public DockerfileWithLabelExtensionImpl(Dockerfile dockerfile) {
        this.dockerfile = dockerfile;
        this.labels = dockerfile.getProject().getObjects().mapProperty(String.class, String.class);
    }

    private void addLabelActionIfNecessary() {
        if (!this.labelActionAdded) {
            this.labelActionAdded = true;
            this.dockerfile.doFirst("Append labels to instructions.", new AddLabelAction());
        }
    }

    @Override
    public void withLabels(Provider<Map<String, String>> labels) {
        this.labels.putAll(labels);
        this.addLabelActionIfNecessary();
    }

    @Override
    public void withLabels(Supplier<Map<String, String>> labels) {
        withLabels(dockerfile.getProject().provider(labels::get));
    }

    @Override
    public void withLabels(Map<String, String> labels) {
        this.labels.putAll(labels);
        this.addLabelActionIfNecessary();
    }

    @Override
    public void withLabels(LabelSupplierTask task) {
        withLabels(task.getLabels());
        this.dockerfile.dependsOn(task);
    }

    public class AddLabelAction implements Action<Task> {

        @Override
        public void execute(Task task) {
            if (task instanceof Dockerfile) {
                execute((Dockerfile) task);
            } else {
                throw new IllegalArgumentException("Task must be a Dockerfile");
            }
        }

        public void execute(Dockerfile task) {
            if (!labels.get().isEmpty()) {
                task.label(labels);
            }
        }
    }
}
