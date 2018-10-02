package eu.xenit.gradle.docker;

import groovy.lang.Closure;

import java.io.File;
import org.gradle.api.Action;
import org.gradle.api.Project;

/**
 * Created by thijs on 10/25/16.
 */
public class DockerFileExtension {
    private File dockerFile;
    private DockerBuildExtension dockerBuild;

    public DockerFileExtension(Project project) {
        dockerFile = project.file("Dockerfile");
        dockerBuild = new DockerBuildExtension(project);
    }

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

    public void dockerBuild(Action<? super DockerBuildExtension> closure) {
        closure.execute(dockerBuild);
    }
}
