package eu.xenit.gradle.docker.compose;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class DockerComposeBasePlugin implements Plugin<Project> {

    private DockerComposeConvention dockerComposeConvention;

    @Override
    public void apply(Project project) {
        dockerComposeConvention = project.getExtensions()
                .create("dockerCompose", DockerComposeExtensionOverride.class, project);
    }

    public DockerComposeConvention getDockerComposeConvention() {
        return dockerComposeConvention;
    }
}
