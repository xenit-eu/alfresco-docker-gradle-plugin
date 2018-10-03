package eu.xenit.gradle.tasks;

import de.schlichtherle.truezip.file.TFile;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static eu.xenit.gradle.alfresco.DockerAlfrescoPlugin.LABEL_PREFIX;

public class InjectFilesInWarTask extends DefaultTask implements WarEnrichmentTask {

    /**
     * WAR file used as input (is not modified)
     */
    private Supplier<File> inputWar;

    /**
     * WAR file used as output (is created from inputWar, and files from sourceFiles placed at targetDirectory)
     */
    private Supplier<File> outputWar = () -> { return getProject().getBuildDir().toPath().resolve("xenit-gradle-plugins").resolve(getName()).resolve(getName()+".war").toFile(); };

    /**
     * Files to inject in the war
     */
    private Supplier<Set<File>> sourceFiles;

    /**
     * Target location to copy the files to inside the war
     */
    private String targetDirectory;

    private List<Supplier<Map<String, String>>> labels = new ArrayList<>();

    @InputFile
    @Override
    public File getInputWar() {
        return inputWar.get();
    }

    @Override
    public void setInputWar(Supplier<File> inputWar) {
        this.inputWar = inputWar;
    }

    @OutputFile
    @Override
    public File getOutputWar() {
        return outputWar.get();
    }

    @Override
    public void setOutputWar(Supplier<File> outputWar) {
        this.outputWar = outputWar;
    }

    @InputFiles
    public Set<File> getSourceFiles()
    {
        return sourceFiles.get();
    }

    public void setSourceFiles(FileCollection fileCollection)
    {
        dependsOn(fileCollection);
        setSourceFiles(fileCollection::getFiles);
    }

    public void setSourceFiles(Task task)
    {
        dependsOn(task);
        setSourceFiles(() -> task.getOutputs().getFiles().getFiles());
    }

    public void setSourceFiles(Supplier<Set<File>> files)
    {
        sourceFiles = files;
    }

    public void setSourceFiles(Set<File> files)
    {
        setSourceFiles(() -> files);
    }

    @Input
    public String getTargetDirectory() {
        return targetDirectory;
    }

    public void setTargetDirectory(String targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    @TaskAction
    public void injectFiles() throws IOException {
        File outputWar = getOutputWar();
        FileUtils.copyFile(getInputWar(), outputWar);
        Util.withWar(outputWar, war -> {
            TFile warJarFolder = new TFile(war.getAbsolutePath() + getTargetDirectory());
            warJarFolder.mkdirs();

            try {
                for (File file : getSourceFiles()) {
                    new TFile(file).cp_rp(new TFile(warJarFolder, file.getName()));
                }
            } catch(IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Override
    public void withLabels(Supplier<Map<String, String>> labels) {
        this.labels.add(labels);
    }

    @Override
    @Internal
    public Map<String, String> getLabels() {
        Map<String, String> accumulator = new HashMap<>();
        String injectedFiles = getSourceFiles()
                .stream()
                .map(File::getName)
                .collect(Collectors.joining(", "));
        accumulator.put(LABEL_PREFIX+getName(), injectedFiles);
        for (Supplier<Map<String, String>> supplier : labels) {
            accumulator.putAll(supplier.get());
        }
        return accumulator;
    }
}
