package eu.xenit.gradle.tasks;

import static eu.xenit.gradle.alfresco.DockerAlfrescoPlugin.LABEL_PREFIX;

import de.schlichtherle.truezip.file.TFile;
import eu.xenit.gradle.docker.internal.Deprecation;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;

/**
 * Created by thijs on 31/05/17.
 * This task can get a configuration as input and resolves it to a file as output. It is mainly used to pass the
 * filename in the labels.
 */
public class StripAlfrescoWarTask extends DefaultTask implements WarEnrichmentTask {

    private FileCollection war;
    private List<Supplier<Map<String, String>>> labels = new ArrayList<>();
    private Set<String> pathsToCopy = new HashSet<>();

    @InputFiles
    @SkipWhenEmpty
    public FileCollection get_internal_inputFiles() {
        return war;
    }

    @Override
    public void set_internal_inputFiles(FileCollection fileCollection) {
        war = fileCollection;
    }

    @Override
    @OutputFile
    public File getOutputWar() {
        if (war.isEmpty()) {
            return null;
        }
        return getProject().getBuildDir().toPath()
                .resolve("xenit-gradle-plugins").resolve(getName())
                .resolve(getName() + ".war").toFile();
    }

    @Override
    public void withLabels(Supplier<Map<String, String>> labels) {
        this.labels.add(labels);
    }

    @Override
    @Internal
    public Map<String, String> getLabels() {
        Map<String, String> accumulator = new HashMap<>();
        if (!war.isEmpty()) {
            accumulator.put(LABEL_PREFIX + getName(), getInputWar().getName());
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
