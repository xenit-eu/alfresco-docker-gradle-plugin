package eu.xenit.gradle.docker;

import groovy.lang.Closure;

import java.io.File;

/**
 * Created by thijs on 10/25/16.
 */
public class DockerFileExtension {
    private File dockerFile;
    private DockerBuildExtension dockerBuild;

    public File getDockerFile() {
        return dockerFile;
    }

    public void setDockerFile(File dockerFile) {
        this.dockerFile = dockerFile;
    }

    public DockerBuildExtension getDockerBuild() {
        return dockerBuild;
    }

    public void setDockerBuild(DockerBuildExtension dockerBuild) {
        this.dockerBuild = dockerBuild;
    }

    public void dockerBuild(Closure closure) {
        dockerBuild = new DockerBuildExtension();
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.setDelegate(dockerBuild);
        closure.call();
    }
}
