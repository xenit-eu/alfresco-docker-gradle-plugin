package eu.xenit.gradle.tasks;

import static eu.xenit.gradle.alfresco.DockerAlfrescoPlugin.LABEL_PREFIX;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.SkipWhenEmpty;

abstract class AbstractInjectFilesInWarTask extends AbstractWarEnrichmentTask {
    /**
     * Files to inject in the war
     */
    private final ConfigurableFileCollection sourceFiles = getProject().files();

    @InputFiles
    @SkipWhenEmpty
    public ConfigurableFileCollection getSourceFiles() {
        return sourceFiles;
    }

    public void setSourceFiles(Object files) {
        sourceFiles.setFrom(files);
    }

    protected void configureLabels() {
        String injectedFiles = getSourceFiles()
                .getFiles()
                .stream()
                .map(File::getName)
                .collect(Collectors.joining(", "));
        getLabels().put(LABEL_PREFIX + getName(), injectedFiles);
    }
}
