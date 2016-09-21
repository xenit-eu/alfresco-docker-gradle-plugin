package eu.xenit.gradle.tasks;

import de.schlichtherle.truezip.file.TFile;
import org.alfresco.repo.module.tool.WarHelperImpl;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Supplier;

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
                    for(String pathToCopy: pathsToCopy)  {
                        TFile fileToCopy = new TFile(inputWar.getAbsolutePath()+pathToCopy);
                        TFile fileToReceive = new TFile(outputWar.getAbsolutePath()+pathToCopy);
                        TFile.cp(fileToCopy, fileToReceive);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        });
    }
}
