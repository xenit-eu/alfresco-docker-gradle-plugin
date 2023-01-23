package eu.xenit.gradle.docker.compose;

import com.avast.gradle.dockercompose.ComposeSettings;
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage;
import java.time.Duration;
import javax.inject.Inject;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskProvider;

abstract class DockerComposeSettingsOverride extends ComposeSettings implements DockerComposeConvention {

    private DockerComposeConvention dockerComposeConvention;

    @Inject
    public DockerComposeSettingsOverride(Project project, String name, String parentName) {
        super(project, name, parentName);
    }

    @Override
    public ComposeSettings cloneAsNested(String name) {
        throw new UnsupportedOperationException("Can not create nested docker-compose settings.");
    }

    @Override
    public void fromBuildImage(String environmentVariable,
            TaskProvider<? extends DockerBuildImage> buildImageTaskProvider) {
        dockerComposeConvention.fromBuildImage(environmentVariable, buildImageTaskProvider);
    }

    @Override
    public void fromBuildImage(String environmentVariable, DockerBuildImage buildImage) {
        dockerComposeConvention.fromBuildImage(environmentVariable, buildImage);
    }

    @Override
    public void fromBuildImage(TaskProvider<? extends DockerBuildImage> buildImageTaskProvider) {
        dockerComposeConvention.fromBuildImage(buildImageTaskProvider);
    }

    @Override
    public void fromBuildImage(DockerBuildImage buildImage) {
        dockerComposeConvention.fromBuildImage(buildImage);
    }

    @Override
    public void fromProject(Project project) {
        dockerComposeConvention.fromProject(project);
    }

    @Override
    public void fromProject(String projectName) {
        dockerComposeConvention.fromProject(projectName);
    }

    @Override
    public void fromProject(String environmentVariable, Project project) {
        dockerComposeConvention.fromProject(environmentVariable, project);
    }

    @Override
    public void fromProject(String environmentVariable, String projectName) {
        dockerComposeConvention.fromProject(environmentVariable, projectName);
    }

    void setDockerComposeConvention(DockerComposeConvention dockerComposeConvention) {
        this.dockerComposeConvention = dockerComposeConvention;
    }
}
