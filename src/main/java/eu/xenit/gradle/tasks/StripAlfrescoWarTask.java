package eu.xenit.gradle.tasks;

import static eu.xenit.gradle.alfresco.DockerAlfrescoPlugin.LABEL_PREFIX;

import de.schlichtherle.truezip.file.TFile;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

/**
 * Created by thijs on 31/05/17.
 * This task can get a configuration as input and resolves it to a file as output. It is mainly used to pass the
 * filename in the labels.
 */
public class StripAlfrescoWarTask extends DefaultTask implements WarEnrichmentTask {

    /**
     * WAR file used as input (is not modified)
     */
    private RegularFileProperty inputWar = getProject().getObjects().fileProperty();

    private RegularFileProperty outputWar = getProject().getObjects().fileProperty()
            .convention(getProject().provider(() -> inputWar.isPresent()?getProject().getLayout().getBuildDirectory().file("xenit-gradle-plugins/"+getName()+"/"+getName()+".war").get():null));

    private List<Supplier<Map<String, String>>> labels = new ArrayList<>();
    private Set<String> pathsToCopy = new HashSet<>();

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
    public void withLabels(Supplier<Map<String, String>> labels) {
        this.labels.add(labels);
    }

    @Override
    @Internal
    public Map<String, String> getLabels() {
        Map<String, String> accumulator = new HashMap<>();
        if (getInputWar().isPresent()) {
            accumulator.put(LABEL_PREFIX + getName(), getInputWar().get().getAsFile().getName());
        }
        for (Supplier<Map<String, String>> supplier : labels) {
            accumulator.putAll(supplier.get());
        }
        return accumulator;
    }

    public void addPathToCopy(String path) {
        pathsToCopy.add(path);
    }

    @TaskAction
    public void copyWar() {
        Util.withWar(getInputWar().getAsFile().get(), inputWar -> {
            Util.withWar(getOutputWar().get().getAsFile(), outputWar -> {
                try {
                    for (String pathToCopy : pathsToCopy) {
                        TFile fileToCopy = new TFile(inputWar.getAbsolutePath() + pathToCopy);
                        TFile fileToReceive = new TFile(outputWar.getAbsolutePath() + pathToCopy);
                        TFile.cp(fileToCopy, fileToReceive);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        });
    }

}
