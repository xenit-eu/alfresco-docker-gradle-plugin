package eu.xenit.gradle.tasks;

import eu.xenit.gradle.alfresco.amp.ModuleDependencySorter;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.alfresco.repo.module.tool.ModuleManagementTool;
import org.apache.commons.io.FileUtils;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Optional;
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
            List<File> filesInInstallationOrder = ModuleDependencySorter.sortByInstallOrder(sourceFiles, outputWar);

            for (File file : filesInInstallationOrder) {
                getLogger().debug("installing amp from {} in war {}", file.getAbsolutePath(), outputWar.getAbsolutePath());
                moduleManagementTool.installModule(file.getAbsolutePath(), outputWar.getAbsolutePath(), false, true, false);
            }

        });
    }
}
