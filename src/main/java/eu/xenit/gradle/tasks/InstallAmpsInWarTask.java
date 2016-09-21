package eu.xenit.gradle.tasks;

import org.alfresco.repo.module.tool.ModuleManagementTool;
import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.Optional;

import java.io.File;
import java.io.IOException;

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

    @Override
    public void injectFiles() throws IOException {
        ModuleManagementTool moduleManagmentTool = new ModuleManagementTool();
        FileUtils.copyFile(getInputWar(), getOutputWar());
        for (File file : getSourceFiles()) {
            if(file.isDirectory()) {
                getLogger().debug("installing amps from "+file.getAbsolutePath()+" in war "+getOutputWar().getAbsolutePath());
                moduleManagmentTool.installModules(file.getAbsolutePath(), getOutputWar().getAbsolutePath(), false, true, false);
            } else {
                getLogger().debug("installing amp from "+file.getAbsolutePath()+" in war "+getOutputWar().getAbsolutePath());
                moduleManagmentTool.installModule(file.getAbsolutePath(), getOutputWar().getAbsolutePath(), false, true, false);
            }
        }
    }
}
