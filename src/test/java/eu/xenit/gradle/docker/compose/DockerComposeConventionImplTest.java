package eu.xenit.gradle.docker.compose;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.avast.gradle.dockercompose.ComposeSettings;
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage;
import eu.xenit.gradle.alfresco.DockerAlfrescoPlugin;
import eu.xenit.gradle.docker.DockerPlugin;
import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;

public class DockerComposeConventionImplTest {

    private static final String DOCKER_IMAGE_ID = "c0ff33";

    private ProjectInternal project;
    private TaskProvider<DockerBuildImage> dockerBuildImageProvider;
    private ComposeSettings composeSettings;
    private DockerComposeConvention composeConvention;

    @Before
    public void setup() {
        project = (ProjectInternal) ProjectBuilder.builder().build();
        dockerBuildImageProvider = createBuildDockerImage(project);
        DockerComposeExtensionOverride extensionOverride = new DockerComposeExtensionOverride(project);
        composeSettings = extensionOverride;
        composeConvention = extensionOverride;
    }

    private TaskProvider<DockerBuildImage> createBuildDockerImage(Project project) {
        TaskProvider<DockerBuildImage> dockerBuildImageProvider = project.getTasks()
                .register("buildDockerImage", DockerBuildImage.class);
        dockerBuildImageProvider.configure(dockerBuildImage -> {
            dockerBuildImage.getImageId().set(DOCKER_IMAGE_ID);
        });
        return dockerBuildImageProvider;
    }

    private void runDockerBuildImage() {
        runDockerBuildImage(dockerBuildImageProvider);
    }

    private void runDockerBuildImage(TaskProvider<DockerBuildImage> dockerBuildImageProvider) {
        dockerBuildImageProvider.get().getTaskActions().forEach(inputChangesAwareTaskAction -> {
            if (inputChangesAwareTaskAction.getDisplayName()
                    .contains(DockerComposeConventionImpl.CONFIGURE_COMPOSE_ACTION_NAME)) {
                inputChangesAwareTaskAction.execute(dockerBuildImageProvider.get());
            }
        });
    }

    @Test
    public void testFromBuildImage() {
        DockerBuildImage dockerBuildImage = dockerBuildImageProvider.get();
        composeConvention.fromBuildImage(dockerBuildImage);

        assertTrue(composeSettings.getUpTask().getDependsOn().contains(dockerBuildImage));
        assertTrue(composeSettings.getBuildTask().getDependsOn().contains(dockerBuildImage));
        assertTrue(composeSettings.getPushTask().getDependsOn().contains(dockerBuildImage));

        runDockerBuildImage();

        assertEquals(DOCKER_IMAGE_ID, composeSettings.getEnvironment().get("BUILD_DOCKER_IMAGE_DOCKER_IMAGE"));
    }

    @Test
    public void testFromBuildImageWithEnvironment() {
        DockerBuildImage dockerBuildImage = dockerBuildImageProvider.get();
        composeConvention.fromBuildImage("DOCKER_IMAGE", dockerBuildImage);

        assertTrue(composeSettings.getUpTask().getDependsOn().contains(dockerBuildImage));
        assertTrue(composeSettings.getBuildTask().getDependsOn().contains(dockerBuildImage));
        assertTrue(composeSettings.getPushTask().getDependsOn().contains(dockerBuildImage));

        runDockerBuildImage();

        assertEquals(DOCKER_IMAGE_ID, composeSettings.getEnvironment().get("DOCKER_IMAGE"));

    }

    @Test
    public void testFromBuildImageProvider() {
        composeConvention.fromBuildImage(dockerBuildImageProvider);

        assertTrue(composeSettings.getUpTask().getDependsOn().contains(dockerBuildImageProvider));
        assertTrue(composeSettings.getBuildTask().getDependsOn().contains(dockerBuildImageProvider));
        assertTrue(composeSettings.getPushTask().getDependsOn().contains(dockerBuildImageProvider));

        runDockerBuildImage();

        assertEquals(DOCKER_IMAGE_ID, composeSettings.getEnvironment().get("BUILD_DOCKER_IMAGE_DOCKER_IMAGE"));
    }

    @Test
    public void testFromBuildImageProviderWithEnvironment() {
        composeConvention.fromBuildImage("DOCKER_IMAGE", dockerBuildImageProvider);

        assertTrue(composeSettings.getUpTask().getDependsOn().contains(dockerBuildImageProvider));
        assertTrue(composeSettings.getBuildTask().getDependsOn().contains(dockerBuildImageProvider));
        assertTrue(composeSettings.getPushTask().getDependsOn().contains(dockerBuildImageProvider));

        runDockerBuildImage();

        assertEquals(DOCKER_IMAGE_ID, composeSettings.getEnvironment().get("DOCKER_IMAGE"));

    }

    @Test
    public void testFromProject() {
        composeConvention.fromProject(project);

        assertTrue(composeSettings.getUpTask().getDependsOn().contains(dockerBuildImageProvider.get()));
        assertTrue(composeSettings.getBuildTask().getDependsOn().contains(dockerBuildImageProvider.get()));
        assertTrue(composeSettings.getPushTask().getDependsOn().contains(dockerBuildImageProvider.get()));

        runDockerBuildImage();

        assertEquals(DOCKER_IMAGE_ID, composeSettings.getEnvironment().get("BUILD_DOCKER_IMAGE_DOCKER_IMAGE"));
    }

    @Test
    public void testFromOtherProject() {
        Project subProject1 = ProjectBuilder.builder().withParent(project).withName("sub-project1").build();
        Project subProject2 = ProjectBuilder.builder().withParent(project).withName("sub-project2").build();

        TaskProvider<DockerBuildImage> subProject2BuildDockerImageProvider = createBuildDockerImage(subProject2);

        composeConvention.fromProject(subProject1);
        composeConvention.fromProject(subProject2);

        assertTrue(composeSettings.getUpTask().getDependsOn().contains(subProject2BuildDockerImageProvider.get()));
        assertTrue(composeSettings.getBuildTask().getDependsOn().contains(subProject2BuildDockerImageProvider.get()));
        assertTrue(composeSettings.getPushTask().getDependsOn().contains(subProject2BuildDockerImageProvider.get()));

        runDockerBuildImage(subProject2BuildDockerImageProvider);
        assertEquals(DOCKER_IMAGE_ID,
                composeSettings.getEnvironment().get("SUB_PROJECT2_BUILD_DOCKER_IMAGE_DOCKER_IMAGE"));

        TaskProvider<DockerBuildImage> subProject1BuildDockerImageProvider = createBuildDockerImage(subProject1);

        assertTrue(composeSettings.getUpTask().getDependsOn().contains(subProject1BuildDockerImageProvider.get()));
        assertTrue(composeSettings.getBuildTask().getDependsOn().contains(subProject1BuildDockerImageProvider.get()));
        assertTrue(composeSettings.getPushTask().getDependsOn().contains(subProject1BuildDockerImageProvider.get()));

        runDockerBuildImage(subProject1BuildDockerImageProvider);
        assertEquals(DOCKER_IMAGE_ID,
                composeSettings.getEnvironment().get("SUB_PROJECT1_BUILD_DOCKER_IMAGE_DOCKER_IMAGE"));
    }

    @Test
    public void testFromProjectWithDockerPlugin() {
        Project subProject1 = ProjectBuilder.builder().withParent(project).withName("sub-project1").build();
        composeConvention.fromProject(subProject1);

        subProject1.getProjectDir().mkdirs();

        subProject1.getPluginManager().apply(DockerPlugin.class);
        TaskProvider<DockerBuildImage> subProject1BuildDockerImageProvider = subProject1.getTasks()
                .named("buildDockerImage", DockerBuildImage.class);
        subProject1BuildDockerImageProvider.configure(buildImage -> {
            buildImage.getImageId().set(DOCKER_IMAGE_ID);
        });
        runDockerBuildImage(subProject1BuildDockerImageProvider);

        assertEquals(DOCKER_IMAGE_ID, composeSettings.getEnvironment().get("SUB_PROJECT1_DOCKER_IMAGE"));
    }

    @Test
    public void testFromProjectWithDockerAlfrescoPlugin() {
        Project subProject1 = ProjectBuilder.builder().withParent(project).withName("sub-project1").build();
        composeConvention.fromProject(subProject1);

        subProject1.getProjectDir().mkdirs();

        subProject1.getPluginManager().apply(DockerAlfrescoPlugin.class);
        TaskProvider<DockerBuildImage> subProject1BuildDockerImageProvider = subProject1.getTasks()
                .named("buildDockerImage", DockerBuildImage.class);
        subProject1BuildDockerImageProvider.configure(buildImage -> {
            buildImage.getImageId().set(DOCKER_IMAGE_ID);
        });
        runDockerBuildImage(subProject1BuildDockerImageProvider);

        assertEquals(DOCKER_IMAGE_ID, composeSettings.getEnvironment().get("SUB_PROJECT1_DOCKER_IMAGE"));
    }


}
