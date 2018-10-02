package eu.xenit.gradle.docker.tasks.internal;

import groovy.lang.Closure;
import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;

public class DockerBuildImage extends com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
{
    private Supplier<File> inputDirSupplier = () -> this.getProject().file("docker");

    private Supplier<File> dockerfileSupplier = () -> getInputDir().toPath().resolve("Dockerfile").toFile();

    private Supplier<Set<String>> tagsSupplier = Collections::emptySet;

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

    @Override
    @Input
    @Optional
    public Set<String> getTags() {
        return tagsSupplier.get();
    }

    @Override
    public void setTags(Set<String> tags) {
        tagsSupplier = () -> tags;
    }

    public void setTags(Closure<Set<String>> tags) {
        tagsSupplier = tags::call;
    }

    public void setTags(Supplier<Set<String>> tagsSupplier) {
        this.tagsSupplier = tagsSupplier;
    }
}
