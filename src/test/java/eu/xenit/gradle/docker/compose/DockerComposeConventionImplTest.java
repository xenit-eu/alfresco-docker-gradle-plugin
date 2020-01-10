package eu.xenit.gradle.docker.compose;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.avast.gradle.dockercompose.ComposeSettings;
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage;
import eu.xenit.gradle.docker.alfresco.DockerAlfrescoPlugin;
import eu.xenit.gradle.docker.DockerPlugin;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.tasks.TaskCollection;
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

    private static Set<Task> resolveDependenciesToTasks(Task task) {
        return task.getDependsOn()
                .stream()
                .flatMap(dependency -> {
                    if (dependency instanceof Task) {
                        return Stream.of((Task) dependency);
                    } else if (dependency instanceof TaskProvider) {
                        return Stream.of(((TaskProvider<Task>) dependency).get());
                    } else if (dependency instanceof TaskCollection) {
                        return ((TaskCollection<Task>) dependency).stream();
                    } else {
                        return Stream.empty();
                    }
                })
                .collect(Collectors.toSet());
    }

    private static Set<Task> resolveDependenciesToTasks(TaskProvider<? extends Task> taskProvider) {
        return resolveDependenciesToTasks(taskProvider.get());
    }

    private static void assertTaskDependsOn(Task task, Task dependency) {
        assertTrue("Task " + task.getPath() + " should contain a dependency on " + dependency.getPath(),
                resolveDependenciesToTasks(task).contains(dependency));
    }

    private static void assertTaskDependsOn(TaskProvider<? extends Task> task, Task dependency) {
        assertTaskDependsOn(task.get(), dependency);
    }

    private static void assertTaskDependsOn(TaskProvider<? extends Task> task,
            TaskProvider<? extends Task> dependency) {
        assertTaskDependsOn(task.get(), dependency.get());
    }

    @Test
    public void testFromBuildImage() {
        DockerBuildImage dockerBuildImage = dockerBuildImageProvider.get();
        composeConvention.fromBuildImage(dockerBuildImage);

        assertTaskDependsOn(composeSettings.getUpTask(), dockerBuildImage);
        assertTaskDependsOn(composeSettings.getBuildTask(), dockerBuildImage);
        assertTaskDependsOn(composeSettings.getPushTask(), dockerBuildImage);

        runDockerBuildImage();

        assertEquals(DOCKER_IMAGE_ID, composeSettings.getEnvironment().get("BUILD_DOCKER_IMAGE_DOCKER_IMAGE"));
    }

    @Test
    public void testFromBuildImageWithEnvironment() {
        DockerBuildImage dockerBuildImage = dockerBuildImageProvider.get();
        composeConvention.fromBuildImage("DOCKER_IMAGE", dockerBuildImage);

        assertTaskDependsOn(composeSettings.getUpTask(), dockerBuildImage);
        assertTaskDependsOn(composeSettings.getBuildTask(), dockerBuildImage);
        assertTaskDependsOn(composeSettings.getPushTask(), dockerBuildImage);

        runDockerBuildImage();

        assertEquals(DOCKER_IMAGE_ID, composeSettings.getEnvironment().get("DOCKER_IMAGE"));

    }

    @Test
    public void testFromBuildImageProvider() {
        composeConvention.fromBuildImage(dockerBuildImageProvider);

        assertTaskDependsOn(composeSettings.getUpTask(), dockerBuildImageProvider);
        assertTaskDependsOn(composeSettings.getBuildTask(), dockerBuildImageProvider);
        assertTaskDependsOn(composeSettings.getPushTask(), dockerBuildImageProvider);

        runDockerBuildImage();

        assertEquals(DOCKER_IMAGE_ID, composeSettings.getEnvironment().get("BUILD_DOCKER_IMAGE_DOCKER_IMAGE"));
    }

    @Test
    public void testFromBuildImageProviderWithEnvironment() {
        composeConvention.fromBuildImage("DOCKER_IMAGE", dockerBuildImageProvider);

        assertTaskDependsOn(composeSettings.getUpTask(), dockerBuildImageProvider);
        assertTaskDependsOn(composeSettings.getBuildTask(), dockerBuildImageProvider);
        assertTaskDependsOn(composeSettings.getPushTask(), dockerBuildImageProvider);

        runDockerBuildImage();

        assertEquals(DOCKER_IMAGE_ID, composeSettings.getEnvironment().get("DOCKER_IMAGE"));

    }

    @Test
    public void testFromProject() {
        composeConvention.fromProject(project);
        TaskCollection<DockerBuildImage> buildImageTaskCollection = project.getTasks().withType(DockerBuildImage.class);

        assertTaskDependsOn(composeSettings.getUpTask(), dockerBuildImageProvider);
        assertTaskDependsOn(composeSettings.getBuildTask(), dockerBuildImageProvider);
        assertTaskDependsOn(composeSettings.getPushTask(), dockerBuildImageProvider);

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

        assertTaskDependsOn(composeSettings.getUpTask(), subProject2BuildDockerImageProvider);
        assertTaskDependsOn(composeSettings.getBuildTask(), subProject2BuildDockerImageProvider);
        assertTaskDependsOn(composeSettings.getPushTask(), subProject2BuildDockerImageProvider);

        runDockerBuildImage(subProject2BuildDockerImageProvider);
        assertEquals(DOCKER_IMAGE_ID,
                composeSettings.getEnvironment().get("SUB_PROJECT2_BUILD_DOCKER_IMAGE_DOCKER_IMAGE"));

        TaskProvider<DockerBuildImage> subProject1BuildDockerImageProvider = createBuildDockerImage(subProject1);

        assertTaskDependsOn(composeSettings.getUpTask(), subProject1BuildDockerImageProvider);
        assertTaskDependsOn(composeSettings.getBuildTask(), subProject1BuildDockerImageProvider);
        assertTaskDependsOn(composeSettings.getPushTask(), subProject1BuildDockerImageProvider);

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
