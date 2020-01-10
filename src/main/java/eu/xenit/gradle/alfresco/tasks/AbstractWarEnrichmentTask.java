package eu.xenit.gradle.alfresco.tasks;

import java.util.Map;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;

public abstract class AbstractWarEnrichmentTask extends DefaultTask implements WarEnrichmentTask {

    /**
     * WAR file used as input (is not modified)
     */
    private RegularFileProperty inputWar = getProject().getObjects().fileProperty();

    private RegularFileProperty outputWar = getProject().getObjects().fileProperty()
            .convention(getProject().provider(() -> inputWar.isPresent() ? getProject().getLayout().getBuildDirectory()
                    .file("xenit-gradle-plugins/" + getName() + "/" + getName() + ".war").get() : null));

    private MapProperty<String, String> labels = getProject().getObjects().mapProperty(String.class, String.class).empty();

    @InputFile
    @Override
    public RegularFileProperty getInputWar() {
        return inputWar;
    }

    @Override
    @OutputFile
    public RegularFileProperty getOutputWar() {
        return outputWar;
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

}
