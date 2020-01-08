package eu.xenit.gradle.docker.compose;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class DockerComposePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getExtensions().create("dockerCompose", DockerComposeExtensionOverride.class, project);
    }
}
