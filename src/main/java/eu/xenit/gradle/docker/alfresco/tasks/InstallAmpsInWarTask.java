package eu.xenit.gradle.docker.alfresco.tasks;

import eu.xenit.gradle.docker.alfresco.internal.amp.ModuleDependencySorter;
import eu.xenit.gradle.docker.alfresco.internal.amp.ModuleInformation;
import eu.xenit.gradle.docker.alfresco.internal.amp.ModuleInstallationException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.module.tool.ModuleManagementTool;
import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.TaskAction;

public class InstallAmpsInWarTask extends AbstractInjectFilesInWarTask {

    private static Stream<File> listFilesRecursively(File file) {
        if (file.isDirectory()) {
            return Arrays.stream(file.listFiles()).flatMap(InstallAmpsInWarTask::listFilesRecursively);
        } else {
            return Stream.of(file);
        }
    }

    @TaskAction
    public void injectFiles() throws IOException {
        // Configure labels
        configureLabels();

        // Install AMMs
        File outputWar = getOutputWar().get().getAsFile();
        FileUtils.copyFile(getInputWar().getAsFile().get(), outputWar);
        Util.withGlobalTvfsLock(() -> {
            ModuleManagementTool moduleManagementTool = new ModuleManagementTool();

            Set<File> sourceFiles = getSourceFiles().getFiles()
                    .stream()
                    .flatMap(InstallAmpsInWarTask::listFilesRecursively)
                    .collect(Collectors.toSet());
            List<ModuleInformation> modulesInInstallationOrder = ModuleDependencySorter.sortByInstallOrder(sourceFiles, outputWar);

            for (ModuleInformation module : modulesInInstallationOrder) {
                getLogger()
                        .debug("installing module {} ({}) in war {}", module.getId(), module.getFile(), outputWar.getAbsolutePath());
                try {
                    moduleManagementTool
                            .installModule(module.getFile().getAbsolutePath(), outputWar.getAbsolutePath(), false, true, false);
                } catch (AlfrescoRuntimeException exception) {
                    throw new ModuleInstallationException(module, exception);
                }
            }

        });
    }
}
