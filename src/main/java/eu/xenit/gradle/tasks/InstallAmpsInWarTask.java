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
import org.gradle.api.tasks.Optional;

public class InstallAmpsInWarTask extends InjectFilesInWarTask {

    @Override
    @Optional
    public String getTargetDirectory() {
        return null;
    }

    @Override
    public void setTargetDirectory(String targetDirectory) {
        throw new UnsupportedOperationException("Target directory is not supported for InstallAmps");
    }

    private static Stream<File> listFilesRecursively(File file) {
        if (file.isDirectory()) {
            return Arrays.stream(file.listFiles()).flatMap(InstallAmpsInWarTask::listFilesRecursively);
        } else {
            return Stream.of(file);
        }
    }

    @Override
    public void injectFiles() throws IOException {
        ModuleManagementTool moduleManagmentTool = new ModuleManagementTool();
        FileUtils.copyFile(getInputWar(), getOutputWar());

        Set<File> sourceFiles = getSourceFiles().stream()
                .flatMap(InstallAmpsInWarTask::listFilesRecursively)
                .collect(Collectors.toSet());
        List<File> filesInInstallationOrder = ModuleDependencySorter.sortByInstallOrder(sourceFiles);

        for (File file : filesInInstallationOrder) {
            getLogger().debug("installing amp from {} in war {}",file.getAbsolutePath(), getOutputWar().getAbsolutePath());
            moduleManagmentTool
                    .installModule(file.getAbsolutePath(), getOutputWar().getAbsolutePath(), false, true, false);
        }
    }
}
