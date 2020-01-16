package eu.xenit.gradle.docker.compose;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class DockerComposePlugin implements Plugin<Project> {

    public static final String PLUGIN_ID = "eu.xenit.docker-compose";
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
