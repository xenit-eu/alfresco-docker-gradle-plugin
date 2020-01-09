package eu.xenit.gradle.docker.compose;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class DockerComposeAutoPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().withType(DockerComposePlugin.class, dockerComposePlugin -> {
            project.allprojects(project1 -> {
                dockerComposePlugin.getDockerComposeConvention().fromProject(project1);
            });
        });

        project.getPlugins().apply(DockerComposePlugin.class);
    }
}
