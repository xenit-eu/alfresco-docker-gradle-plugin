package eu.xenit.gradle.tasks;

import static eu.xenit.gradle.alfresco.DockerAlfrescoPlugin.LABEL_PREFIX;

import de.schlichtherle.truezip.file.TFile;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;

public class InjectFilesInWarTask extends DefaultTask implements WarEnrichmentTask {

    /**
     * WAR file used as input (is not modified)
     */
    private FileCollection inputWar;

    /**
     * Files to inject in the war
     */
    private Supplier<Set<File>> sourceFiles;

    /**
     * Target location to copy the files to inside the war
     */
    private String targetDirectory;

    private List<Supplier<Map<String, String>>> labels = new ArrayList<>();

    @InputFiles
    @SkipWhenEmpty
    public FileCollection get_internal_inputFiles() {
        return inputWar;
    }

    @Override
    public void set_internal_inputFiles(FileCollection fileCollection) {
        inputWar = fileCollection;
    }

    @OutputFile
    @Override
    public File getOutputWar() {
        if (inputWar.isEmpty()) {
            return null;
        }
        return getProject().getBuildDir().toPath().resolve("xenit-gradle-plugins").resolve(getName())
                .resolve(getName() + ".war").toFile();
    }

    @InputFiles
    @SkipWhenEmpty
    public Set<File> getSourceFiles() {
        return sourceFiles.get();
    }

    public void setSourceFiles(FileCollection fileCollection) {
        dependsOn(fileCollection);
        setSourceFiles(fileCollection::getFiles);
    }

    public void setSourceFiles(Task task) {
        dependsOn(task);
        setSourceFiles(() -> task.getOutputs().getFiles().getFiles());
    }

    public void setSourceFiles(Supplier<Set<File>> files) {
        sourceFiles = files;
    }

    public void setSourceFiles(Set<File> files) {
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
