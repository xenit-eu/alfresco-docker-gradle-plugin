package eu.xenit.gradle.docker.alfresco.tasks;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Zip;

public class MergeWarsTask extends Zip implements LabelConsumerTask, LabelSupplierTask {

    private MapProperty<String, String> labels = getProject().getObjects().mapProperty(String.class, String.class);

    private final CopySpec childWars;

    public MergeWarsTask() {
        super();
        getArchiveExtension().set("war");
        getDestinationDirectory()
                .set(getProject().getLayout().getBuildDirectory().dir("xenit-gradle-plugins/" + getName()));
        getArchiveBaseName().set(getName());
        childWars = getRootSpec().addChildBeforeSpec(getMainSpec());
        childWars.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);
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
