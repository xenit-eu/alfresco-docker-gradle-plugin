package eu.xenit.gradle.docker;

import eu.xenit.gradle.docker.config.DockerConfigPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Created by thijs on 10/25/16.
 */
public class DockerPlugin implements Plugin<Project> {
    public static final String PLUGIN_ID = "eu.xenit.docker";

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(DockerConfigPlugin.class);
        DockerFileExtension dockerFileExtension = project.getExtensions()
                .create("dockerFile", DockerFileExtension.class, project);
        DockerBuildBehavior dockerBuildBehavior = new DockerBuildBehavior(dockerFileExtension.getDockerBuild(),
                dockerFileExtension.getDockerFile());
        dockerBuildBehavior.apply(project);
    }
}
