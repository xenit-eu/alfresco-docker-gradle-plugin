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
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;

abstract class AbstractInjectFilesInWarTask extends DefaultTask implements WarEnrichmentTask {

    /**
     * WAR file used as input (is not modified)
     */
    private RegularFileProperty inputWar = getProject().getObjects().fileProperty();

    private RegularFileProperty outputWar = getProject().getObjects().fileProperty().convention(getProject().getLayout().getBuildDirectory().file("xenit-gradle-plugins/"+getName()+"/"+getName()+".war"));
    /**
     * Files to inject in the war
     */
    private final ConfigurableFileCollection sourceFiles = getProject().files();
    private final List<Supplier<Map<String, String>>> labels = new ArrayList<>();

    @InputFile
    @Override
    public RegularFileProperty getInputWar() {
        return inputWar;
    }

    @OutputFile
    @Override
    public RegularFileProperty getOutputWar() {
        return outputWar;
    }

    @InputFiles
    @SkipWhenEmpty
    public ConfigurableFileCollection getSourceFiles() {
        return sourceFiles;
    }

    public void setSourceFiles(FileCollection files) {
        sourceFiles.setFrom(files);
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
                .getFiles()
                .stream()
                .map(File::getName)
                .collect(Collectors.joining(", "));
        accumulator.put(LABEL_PREFIX + getName(), injectedFiles);
        for (Supplier<Map<String, String>> supplier : labels) {
            accumulator.putAll(supplier.get());
        }
        return accumulator;
    }
}
