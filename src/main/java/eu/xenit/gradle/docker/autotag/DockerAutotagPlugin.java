package eu.xenit.gradle.docker.autotag;

import eu.xenit.gradle.docker.core.DockerExtension;
import eu.xenit.gradle.docker.core.DockerPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class DockerAutotagPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().withType(DockerPlugin.class, dockerPlugin -> {
            DockerExtension dockerExtension = dockerPlugin.getExtension();
            dockerExtension.getExtensions().create("autotag", DockerAutotagExtension.class);
        });
    }
}
