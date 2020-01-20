package eu.xenit.gradle.docker.compose;

import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ReplayableComposeConventionImplTest {
    private DockerComposeConvention dockerComposeConvention;

    private ReplayableComposeConventionImpl replayableComposeConvention;

    @Before
    public void setup() {
        dockerComposeConvention = Mockito.mock(DockerComposeConvention.class);
        replayableComposeConvention = new ReplayableComposeConventionImpl(dockerComposeConvention);
    }

    @Test
    public void testReplay() {
        DockerBuildImage dockerBuildImage = Mockito.mock(DockerBuildImage.class);
        TaskProvider<? extends DockerBuildImage> buildImageTaskProvider = Mockito.mock(TaskProvider.class);
        Project project = Mockito.mock(Project.class);
        replayableComposeConvention.fromBuildImage(dockerBuildImage);
        replayableComposeConvention.fromBuildImage("XYZ", dockerBuildImage);
        replayableComposeConvention.fromBuildImage(buildImageTaskProvider);
        replayableComposeConvention.fromBuildImage("ABC", buildImageTaskProvider);
        replayableComposeConvention.fromProject(project);
        replayableComposeConvention.fromProject("XYZ");

        Mockito.verify(dockerComposeConvention).fromBuildImage(dockerBuildImage);
        Mockito.verify(dockerComposeConvention).fromBuildImage("XYZ", dockerBuildImage);
        Mockito.verify(dockerComposeConvention).fromBuildImage(buildImageTaskProvider);
        Mockito.verify(dockerComposeConvention).fromBuildImage("ABC", buildImageTaskProvider);
        Mockito.verify(dockerComposeConvention).fromProject(project);
        Mockito.verify(dockerComposeConvention).fromProject("XYZ");

        DockerComposeConvention childComposeConvention = Mockito.mock(DockerComposeConvention.class);

        replayableComposeConvention.replayChangesInto(childComposeConvention);

        Mockito.verify(childComposeConvention).fromBuildImage(dockerBuildImage);
        Mockito.verify(childComposeConvention).fromBuildImage("XYZ", dockerBuildImage);
        Mockito.verify(childComposeConvention).fromBuildImage(buildImageTaskProvider);
        Mockito.verify(childComposeConvention).fromBuildImage("ABC", buildImageTaskProvider);
        Mockito.verify(childComposeConvention).fromProject(project);
        Mockito.verify(childComposeConvention).fromProject("XYZ");
    }

}
