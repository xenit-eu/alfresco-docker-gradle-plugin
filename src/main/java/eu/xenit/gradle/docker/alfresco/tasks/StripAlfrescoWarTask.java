package eu.xenit.gradle.docker.alfresco.tasks;

import static eu.xenit.gradle.docker.alfresco.DockerAlfrescoPlugin.LABEL_PREFIX;

import de.schlichtherle.truezip.file.TFile;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.gradle.api.file.FileTree;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

/**
 * Created by thijs on 31/05/17.
 * This task can get a configuration as input and resolves it to a file as output. It is mainly used to pass the
 * filename in the labels.
 */
public class StripAlfrescoWarTask extends AbstractWarEnrichmentTask {

    private static final Logger LOGGER = Logging.getLogger(StripAlfrescoWarTask.class);

    private SetProperty<String> pathsToCopy = getProject().getObjects().setProperty(String.class);

    @Input
    public SetProperty<String> getPathsToCopy() {
        return pathsToCopy;
    }

    public void addPathToCopy(String path) {
        pathsToCopy.add(path);
    }

    @TaskAction
    public void copyWar() {
        getLabels().put(LABEL_PREFIX + getName(), getInputWar().map(f -> f.getAsFile().getName()));
        FileTree filesToInclude = getProject().zipTree(getInputWar())
                .matching(patternFilterable -> patternFilterable.include(pathsToCopy.get()))
                .getAsFileTree();
        Util.withWar(getOutputWar().get().getAsFile(), outputWar -> {
            filesToInclude.visit(fileVisitDetails -> {
                if (!fileVisitDetails.isDirectory()) {
                    LOGGER.debug("Copying file {}", fileVisitDetails.getRelativePath());
                    TFile fileToReceive = new TFile(fileVisitDetails.getRelativePath().prepend(outputWar.getAbsolutePath()).getPathString());
                    try {
                        TFile.cp(fileVisitDetails.getFile(), fileToReceive);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }

                }
            });
        });
    }

}
