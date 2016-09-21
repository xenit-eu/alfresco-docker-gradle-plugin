package eu.xenit.gradle.tasks;

import de.schlichtherle.truezip.file.TFile;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Supplier;

public class MergeWarsTask extends DefaultTask implements LabelConsumerTask, WarLabelOutputTask {
    /**
     * WAR files used as input (are not modified)
     *
     * Later files overwrite earlier files
     */
    private Supplier<List<File>> inputWars;

    /**
     * WAR file used as output (is created from inputWar)
     */
    private Supplier<File> outputWar = () -> { return getProject().getBuildDir().toPath().resolve("xenit-gradle-plugins").resolve(getName()).resolve(getName()+".war").toFile(); };

    private List<Supplier<Map<String, String>>> labels = new ArrayList<>();

    @Override
    public void withLabels(Supplier<Map<String, String>> labels) {
        this.labels.add(labels);
    }

    @Override
    @Internal
    public Map<String, String> getLabels() {
        Map<String, String> accumulator = new HashMap<>();
        for (Supplier<Map<String, String>> supplier : labels) {
            accumulator.putAll(supplier.get());
        }
        return accumulator;
    }

    @InputFiles
    public List<File> getInputWars() {
        return inputWars.get();
    }

    public void setInputWars(Supplier<List<File>> inputWars) {
        this.inputWars = inputWars;
    }

    @Override
    @OutputFile
    public File getOutputWar() {
        return outputWar.get();
    }

    @Override
    public void setOutputWar(Supplier<File> outputWar) {
        this.outputWar = outputWar;
    }

    @TaskAction
    public void stripWar() {
        for(File file: getInputWars()) {
            Util.withWar(file, inputWar -> {
                Util.withWar(getOutputWar(), outputWar -> {
                    try {
                        TFile.cp_rp(inputWar, outputWar, inputWar.getArchiveDetector(), outputWar.getArchiveDetector());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            });
        }
    }

}
