package eu.xenit.gradle.alfresco.tasks;

import java.io.File;
import java.util.Map;
import org.gradle.api.file.CopySpec;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Internal;
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
     */
    public void addInputWar(Provider<File> inputWar) {
        childWars.from(getProject().provider(() -> getProject().zipTree(inputWar)));
    }
}
