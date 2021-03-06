package eu.xenit.gradle.docker.alfresco.tasks.extension;

import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import eu.xenit.gradle.docker.alfresco.tasks.WarLabelOutputTask;
import eu.xenit.gradle.docker.alfresco.tasks.WarOutputTask;
import java.io.File;
import java.util.function.Supplier;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskProvider;

public interface DockerfileWithWarsExtension {

    @Input
    Property<String> getTargetDirectory();

    @Input
    Property<Boolean> getRemoveExistingWar();

    @Input
    Property<Boolean> getCheckAlfrescoVersion();

    @Input
    Property<String> getBaseImage();

    void addWar(String name, WarLabelOutputTask task);

    void addWar(String name, Provider<RegularFile> regularFileProvider);

    void addWar(String name, java.io.File file);

    /**
     * @deprecated since 5.1.0. Will be removed in 6.0.0. Use {@link #addWar(String, Provider)} instead
     */
    @Deprecated
    void addWar(String name, Supplier<File> file);

    void addWar(String name, Configuration configuration);

    default void addWar(String name, TaskProvider<? extends WarLabelOutputTask> taskProvider) {
        addWar(name, taskProvider.flatMap(WarOutputTask::getOutputWar));
    }

    static DockerfileWithWarsExtension get(Dockerfile task) {
        return task.getConvention().getPlugin(DockerfileWithWarsExtension.class);
    }
}
