package eu.xenit.gradle.docker;

import eu.xenit.gradle.docker.core.DockerExtension;
import eu.xenit.gradle.docker.internal.Deprecation;
import eu.xenit.gradle.docker.tasks.internal.DockerBuildImage;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @deprecated Use {@link eu.xenit.gradle.docker.core.DockerPlugin} instead. Will be removed in 6.0
 */
@Deprecated
public class DockerLegacyPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        DockerExtension dockerExtension = project.getExtensions().getByType(DockerExtension.class);

        DockerFileExtension dockerFileExtension = project.getExtensions()
                .create("dockerFile", DockerFileExtension.class, dockerExtension);

        project.getTasks().withType(DockerBuildImage.class).configureEach(buildImage -> {
            Deprecation.whileDisabled(() -> {
                DockerBuildExtension dockerBuildExtension = dockerFileExtension.getDockerBuild();
                buildImage.getRemove().set(dockerBuildExtension.getRemove());
                buildImage.getNoCache().set(dockerBuildExtension.getNoCache());
                buildImage.getPull().set(dockerBuildExtension.getPull());
            });
        });
    }
}
