package eu.xenit.gradle.docker.tasks.internal;

import groovy.lang.Closure;
import java.io.File;
import java.util.function.Supplier;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;

public class DockerBuildImage extends com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
{
    private Supplier<File> inputDirSupplier;

    private Supplier<File> dockerfileSupplier;

    @Override
    public void setInputDir(File file) {
        inputDirSupplier = () -> file;
    }

    public void setInputDir(Supplier<File> supplier) {
        inputDirSupplier = supplier;
    }

    public void setInputDir(Closure<File> closure) {
        inputDirSupplier = closure::call;
    }

    @Override
    @InputDirectory
    public File getInputDir(){
        return this.inputDirSupplier.get();
    }


    @Override
    public void setDockerFile(File dockerfile) {
        dockerfileSupplier = () -> dockerfile;
    }

    public void setDockerFile(Supplier<File> dockerfile) {
        dockerfileSupplier = dockerfile;
    }

    public void setDockerFile(Closure<File> dockerfile) {
        dockerfileSupplier = dockerfile::call;
    }

    @Override
    @InputFile
    @Optional
    public File getDockerFile() {
        return dockerfileSupplier.get();
    }



}
