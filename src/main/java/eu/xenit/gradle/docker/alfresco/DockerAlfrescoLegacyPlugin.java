package eu.xenit.gradle.docker.alfresco;

import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import eu.xenit.gradle.docker.DockerBuildExtension;
import eu.xenit.gradle.docker.alfresco.tasks.InjectFilesInWarTask;
import eu.xenit.gradle.docker.alfresco.tasks.InstallAmpsInWarTask;
import eu.xenit.gradle.docker.alfresco.tasks.MergeWarsTask;
import eu.xenit.gradle.docker.alfresco.tasks.PrefixLog4JWarTask;
import eu.xenit.gradle.docker.alfresco.tasks.StripAlfrescoWarTask;
import eu.xenit.gradle.docker.alfresco.tasks.WarEnrichmentTask;
import eu.xenit.gradle.docker.alfresco.tasks.WarLabelOutputTask;
import eu.xenit.gradle.docker.alfresco.tasks.extension.DockerfileWithWarsExtension;
import eu.xenit.gradle.docker.alfresco.tasks.extension.LabelConsumerExtension;
import eu.xenit.gradle.docker.alfresco.tasks.extension.internal.DockerfileWithLabelExtensionImpl;
import eu.xenit.gradle.docker.alfresco.tasks.extension.internal.DockerfileWithWarsExtensionImpl;
import eu.xenit.gradle.docker.core.DockerExtension;
import eu.xenit.gradle.docker.core.DockerPlugin;
import eu.xenit.gradle.docker.internal.Deprecation;
import eu.xenit.gradle.docker.tasks.internal.DockerBuildImage;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.repo.module.tool.WarHelperImpl;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.TaskProvider;

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
