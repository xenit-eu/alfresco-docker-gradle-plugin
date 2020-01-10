package eu.xenit.gradle.docker.compose;

import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

public interface DockerComposeConvention {
    void fromBuildImage(String environmentVariable, TaskProvider<DockerBuildImage> buildImageTaskProvider);
    void fromBuildImage(String environmentVariable, DockerBuildImage buildImage);

    void fromBuildImage(TaskProvider<DockerBuildImage> buildImageTaskProvider);
    void fromBuildImage(DockerBuildImage buildImage);


    void fromProject(Project project);
    void fromProject(String projectName);
}
