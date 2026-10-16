package eu.xenit.gradle.docker.alfresco.tasks;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import javax.inject.Inject;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.War;

public abstract class MergeWarsTask extends War implements LabelConsumerTask, LabelSupplierTask {

    private final MapProperty<String, String> labels = getProject().getObjects().mapProperty(String.class, String.class);

    private final CopySpec childWars;
    private final DirectoryProperty webAppDirectory;
    private final RegularFileProperty webXmlFile;
    private final Property<Long> reproducibleFileTimestamp;

    public MergeWarsTask() {
        super();
        webAppDirectory = getProject().getObjects().directoryProperty();
        webAppDirectory.convention(getProject().getLayout().getProjectDirectory().dir("src/main/webapp"));
        webXmlFile = getProject().getObjects().fileProperty();
        reproducibleFileTimestamp = getProject().getObjects().property(Long.class);
        getDestinationDirectory()
                .set(getProject().getLayout().getBuildDirectory().dir("xenit-gradle-plugins/" + getName()));
        getArchiveBaseName().set(getName());
        childWars = getRootSpec().addChildBeforeSpec(getMainSpec());
        childWars.setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);
    }

    @Inject
    @Override
    public ObjectFactory getObjectFactory() {
        return getProject().getObjects();
    }

    @Override
    @Internal
    public DirectoryProperty getWebAppDirectory() {
        return webAppDirectory;
    }

    @Override
    @Optional
    @InputFile
    public RegularFileProperty getWebXmlFile() {
        return webXmlFile;
    }

    @Override
    @Optional
    @Input
    public Property<Long> getReproducibleFileTimestamp() {
        return reproducibleFileTimestamp;
    }

    @Override
    public void withLabels(Provider<Map<String, String>> labels) {
        this.labels.putAll(labels);
    }

    @Override
    @Internal
    public MapProperty<String, String> getLabels() {
        return labels;
    }

    /**
     * WAR files used as input (are not modified)
     * <p>
     * Later files overwrite earlier files
     *
     * @param inputWar Provider of a file to be merged
     */
    public void addInputWar(Provider<File> inputWar) {
        getInputs().file(inputWar).optional().skipWhenEmpty();
        childWars.from(inputWar.map(war -> getProject().zipTree(war)));
    }

    public void addInputWar(WarOutputTask inputWar) {
        if (inputWar instanceof WarLabelOutputTask) {
            withLabels((WarLabelOutputTask) inputWar);
        }
        addInputWar(inputWar.getOutputWar().getAsFile());
    }

    public void addInputWar(TaskProvider<? extends WarOutputTask> inputWarProvider) {
        addInputWar(inputWarProvider.flatMap(WarOutputTask::getOutputWar).map(RegularFile::getAsFile));
        withLabels(inputWarProvider.flatMap(t -> {
            if (t instanceof WarLabelOutputTask) {
                return ((WarLabelOutputTask) t).getLabels();
            }
            return getProject().provider(Collections::emptyMap);
        }));
    }
}
