package eu.xenit.gradle.docker.alfresco.tasks;

import eu.xenit.gradle.docker.internal.GradleVersionRequirement;
import java.util.Map;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.SkipWhenEmpty;

public abstract class AbstractWarEnrichmentTask extends DefaultTask implements WarEnrichmentTask {

    /**
     * WAR file used as input (is not modified)
     */
    private RegularFileProperty inputWar = getProject().getObjects().fileProperty();

    private RegularFileProperty outputWar = getProject().getObjects().fileProperty();

    private MapProperty<String, String> labels = getProject().getObjects().mapProperty(String.class, String.class)
            .empty();

    protected AbstractWarEnrichmentTask() {
        outputWar.set(inputWar.flatMap(_x -> getProject().getLayout().getBuildDirectory()
                .file("xenit-gradle-plugins/" + getName() + "/" + getName() + ".war"))
                .map(outputFile -> isEnabled() ? outputFile
                        : GradleVersionRequirement.atLeast("6.2", "disable the " + getName() + " task", () -> null))
        );
    }

    @InputFile
    @SkipWhenEmpty
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
