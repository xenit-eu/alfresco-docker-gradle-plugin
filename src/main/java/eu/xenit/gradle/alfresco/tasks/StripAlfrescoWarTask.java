package eu.xenit.gradle.alfresco.tasks;

import static eu.xenit.gradle.alfresco.DockerAlfrescoPlugin.LABEL_PREFIX;

import de.schlichtherle.truezip.file.TFile;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

/**
 * Created by thijs on 31/05/17.
 * This task can get a configuration as input and resolves it to a file as output. It is mainly used to pass the
 * filename in the labels.
 */
public class StripAlfrescoWarTask extends AbstractWarEnrichmentTask {

    private SetProperty<String> pathsToCopy = getProject().getObjects().setProperty(String.class);

    public StripAlfrescoWarTask() {
        getLabels().put(LABEL_PREFIX + getName(), getInputWar().map(f -> f.getAsFile().getName()));
    }

    @Input
    public SetProperty<String> getPathsToCopy() {
        return pathsToCopy;
    }

    public void addPathToCopy(String path) {
        pathsToCopy.add(path);
    }

    @TaskAction
    public void copyWar() {
        Util.withWar(getInputWar().getAsFile().get(), inputWar -> {
            Util.withWar(getOutputWar().get().getAsFile(), outputWar -> {
                try {
                    for (String pathToCopy : pathsToCopy.get()) {
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
