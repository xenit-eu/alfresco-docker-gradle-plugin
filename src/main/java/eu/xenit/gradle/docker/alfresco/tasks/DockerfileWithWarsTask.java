package eu.xenit.gradle.docker.alfresco.tasks;

import eu.xenit.gradle.docker.alfresco.DockerAlfrescoPlugin;
import eu.xenit.gradle.docker.alfresco.tasks.extension.internal.DockerfileWithLabelExtensionImpl;
import eu.xenit.gradle.docker.alfresco.tasks.extension.internal.DockerfileWithWarsExtensionImpl;
import eu.xenit.gradle.docker.alfresco.tasks.extension.internal.DockerfileWithWarsExtensionImpl.ElideDuplicateVersionChecksAction;
import eu.xenit.gradle.docker.alfresco.tasks.extension.internal.DockerfileWithWarsExtensionImpl.RemoveNoOpInstructionsAction;
import eu.xenit.gradle.docker.alfresco.tasks.extension.DockerfileWithWarsExtension;
import eu.xenit.gradle.docker.alfresco.tasks.extension.LabelConsumerExtension;
import eu.xenit.gradle.docker.internal.Deprecation;
import eu.xenit.gradle.docker.tasks.DockerfileWithCopyTask;
import java.util.Map;
import java.util.function.Supplier;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskProvider;

/**
 * @deprecated since 5.1.0, will be removed in 6.0.0. Use {@link eu.xenit.gradle.docker.alfresco.tasks.extension.DockerfileWithWarsExtension} instead.
 */
@Deprecated
public class DockerfileWithWarsTask extends DockerfileWithCopyTask {

    private final DockerfileWithWarsExtension warsExtension;
    private final LabelConsumerExtension labelConsumerExtension;

    public DockerfileWithWarsTask() {
        super();
        Deprecation.warnDeprecation(
                getClass().getCanonicalName() + " is deprecated. Create a task of type " + DockerfileWithCopyTask.class
                        .getCanonicalName() + " instead and apply the " + DockerAlfrescoPlugin.PLUGIN_ID +
                        " plugin instead.");
        this.labelConsumerExtension = DockerfileWithLabelExtensionImpl.applyToAndGet(this);
        this.warsExtension = DockerfileWithWarsExtensionImpl.applyToAndGet(this);

        getProject().afterEvaluate(p -> {
            this.doFirst("Remove no-op instructions", new RemoveNoOpInstructionsAction());
            this.doFirst("Elide duplicate version check instructions", new ElideDuplicateVersionChecksAction());
        });
    }

    @Input
    public Property<String> getTargetDirectory() {
        return warsExtension.getTargetDirectory();
    }

    @Input
    public Property<Boolean> getRemoveExistingWar() {
        return warsExtension.getRemoveExistingWar();
    }

    @Input
    public Property<Boolean> getCheckAlfrescoVersion() {
        return warsExtension.getCheckAlfrescoVersion();
    }

    @Input
    public Property<String> getBaseImage() {
        return warsExtension.getBaseImage();
    }

    public void addWar(String name, WarLabelOutputTask task) {
        warsExtension.addWar(name, task);
    }

    public void addWar(String name, Provider<RegularFile> regularFileProvider) {
        warsExtension.addWar(name, regularFileProvider);
    }

    public void addWar(String name, java.io.File file) {
        warsExtension.addWar(name, file);
    }

    public void addWar(String name, Configuration configuration) {
        warsExtension.addWar(name, configuration);
    }

    public void withLabels(Provider<Map<String, String>> labels) {
        labelConsumerExtension.withLabels(labels);
    }

    public void withLabels(Supplier<Map<String, String>> labels) {
        labelConsumerExtension.withLabels(labels);
    }

    public void withLabels(Map<String, String> labels) {
        labelConsumerExtension.withLabels(labels);
    }

    public void withLabels(LabelSupplierTask task) {
        labelConsumerExtension.withLabels(task);
    }

    public void withLabels(TaskProvider<? extends LabelSupplierTask> task) {
        labelConsumerExtension.withLabels(task);
    }
}
