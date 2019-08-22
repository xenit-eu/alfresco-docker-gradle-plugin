package eu.xenit.gradle.tasks;

import de.schlichtherle.truezip.file.TFile;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Set;
import org.gradle.api.tasks.TaskAction;

public class StripAlfrescoWarTask extends ResolveWarTask {

    private Set<String> pathsToCopy = new HashSet<>();

    public void addPathToCopy(String path) {
        pathsToCopy.add(path);
    }

    @TaskAction
    @Override
    public void copyWar() {
        Util.withWar(getInputWar(), inputWar -> {
            Util.withWar(getOutputWar(), outputWar -> {
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
