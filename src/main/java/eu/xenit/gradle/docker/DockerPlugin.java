package eu.xenit.gradle.docker;

import eu.xenit.gradle.docker.config.DockerConfigPlugin;
import eu.xenit.gradle.docker.core.DockerExtension;
import eu.xenit.gradle.docker.internal.Deprecation;
import eu.xenit.gradle.docker.tasks.internal.DockerBuildImage;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class DockerPlugin implements Plugin<Project> {
    public static final String PLUGIN_ID = "eu.xenit.docker";

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(eu.xenit.gradle.docker.core.DockerPlugin.class);

        DockerExtension dockerExtension = project.getExtensions().getByType(DockerExtension.class);

        DockerFileExtension dockerFileExtension = project.getExtensions()
                .create("dockerFile", DockerFileExtension.class, dockerExtension);

        project.getTasks().withType(DockerBuildImage.class, buildImage -> {
            Deprecation.whileDisabled(() -> {
                DockerBuildExtension dockerBuildExtension = dockerFileExtension.getDockerBuild();
                buildImage.getRemove().set(dockerBuildExtension.getRemove());
                buildImage.getNoCache().set(dockerBuildExtension.getNoCache());
                buildImage.getPull().set(dockerBuildExtension.getPull());
            });
        });
    }
}
