package eu.xenit.gradle.docker.alfresco;

import eu.xenit.gradle.docker.DockerBuildExtension;
import eu.xenit.gradle.docker.core.DockerExtension;
import eu.xenit.gradle.docker.internal.Deprecation;
import eu.xenit.gradle.docker.tasks.internal.DockerBuildImage;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @deprecated Use {@link DockerAlfrescoPlugin} instead. Will be removed in 6.0
 */
@Deprecated
public class DockerAlfrescoLegacyPlugin implements Plugin<Project> {

    public void apply(Project project) {
        DockerExtension dockerExtension = project.getExtensions().getByType(DockerExtension.class);

        DockerAlfrescoExtension dockerAlfrescoExtension = project.getExtensions()
                .create("dockerAlfresco", DockerAlfrescoExtension.class, dockerExtension);

        // process legacy DockerBuildExtension
        project.getTasks().withType(DockerBuildImage.class).configureEach(buildImage -> {
            Deprecation.whileDisabled(() -> {
                DockerBuildExtension dockerBuildExtension = dockerAlfrescoExtension.getDockerBuild();
                buildImage.getRemove().set(dockerBuildExtension.getRemove());
                buildImage.getNoCache().set(dockerBuildExtension.getNoCache());
                buildImage.getPull().set(dockerBuildExtension.getPull());
            });
        });

    }
}
