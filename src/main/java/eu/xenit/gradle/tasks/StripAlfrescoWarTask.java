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
public class StripAlfrescoWarTask extends AbstractWarEnrichmentTask {

    private Set<String> pathsToCopy = new HashSet<>();

    public StripAlfrescoWarTask() {
        getLabels().put(LABEL_PREFIX + getName(), getInputWar().map(f -> f.getAsFile().getName()));
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
