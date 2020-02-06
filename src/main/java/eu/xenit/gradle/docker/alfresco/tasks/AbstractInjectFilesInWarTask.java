package eu.xenit.gradle.docker.alfresco.tasks;

import static eu.xenit.gradle.docker.alfresco.DockerAlfrescoPlugin.LABEL_PREFIX;

import java.io.File;
import java.util.stream.Collectors;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.InputFiles;
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
        // Do not access field to work around https://github.com/xenit-eu/alfresco-docker-gradle-plugin/issues/98
        getSourceFiles().setFrom(files);
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
