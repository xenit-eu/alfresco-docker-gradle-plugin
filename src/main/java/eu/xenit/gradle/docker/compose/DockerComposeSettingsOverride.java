package eu.xenit.gradle.docker.compose;

import com.avast.gradle.dockercompose.ComposeSettings;
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

public class DockerComposeSettingsOverride extends ComposeSettings implements DockerComposeConvention {
    private DockerComposeConvention dockerComposeConvention;

    public DockerComposeSettingsOverride(Project project, String name) {
        super(project, name);
    }

    @Override
    public ComposeSettings createNested(String name) {
        throw new UnsupportedOperationException("Can not create nested docker-compose settings.");
    }

    @Override
    public void fromBuildImage(String environmentVariable, TaskProvider<? extends DockerBuildImage> buildImageTaskProvider) {
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

    void setDockerComposeConvention(DockerComposeConvention dockerComposeConvention) {
        this.dockerComposeConvention = dockerComposeConvention;
    }
}
