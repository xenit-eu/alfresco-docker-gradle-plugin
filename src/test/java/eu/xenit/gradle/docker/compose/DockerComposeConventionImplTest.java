package eu.xenit.gradle.docker.compose;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.avast.gradle.dockercompose.ComposeSettings;
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage;
import eu.xenit.gradle.docker.alfresco.DockerAlfrescoPlugin;
import eu.xenit.gradle.docker.core.DockerPlugin;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.gradle.api.DefaultTask;
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
        dockerBuildImageProvider = createBuildDockerImage(project, DOCKER_IMAGE_ID);
        DockerComposeExtensionOverride extensionOverride = project.getExtensions().create("bla", DockerComposeExtensionOverride.class);
        composeSettings = extensionOverride;
        composeConvention = extensionOverride;
    }

    private TaskProvider<DockerBuildImage> createBuildDockerImage(Project project, String dockerImageId) {
        TaskProvider<DockerBuildImage> dockerBuildImageProvider = project.getTasks()
                .register("buildDockerImage", DockerBuildImage.class);
        dockerBuildImageProvider.configure(dockerBuildImage -> {
            dockerBuildImage.getImageId().set(dockerImageId);
        });
        return dockerBuildImageProvider;
    }

    private void runSetupImageEnvironmentWithComposeUp() {
        runSetupImageEnvironment(composeSettings.getTasksConfigurator().getUpTask());
    }

    private void runSetupImageEnvironment(TaskProvider<? extends DefaultTask> taskProvider) {
        taskProvider.get().getTaskActions().forEach(inputChangesAwareTaskAction -> {
            if (inputChangesAwareTaskAction.getDisplayName()
                    .contains(DockerComposeConventionImpl.CONFIGURE_COMPOSE_ACTION_NAME)) {
                inputChangesAwareTaskAction.execute(taskProvider.get());
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

        assertTaskDependsOn(composeSettings.getTasksConfigurator().getUpTask(), dockerBuildImage);
        assertTaskDependsOn(composeSettings.getTasksConfigurator().getBuildTask(), dockerBuildImage);
        assertTaskDependsOn(composeSettings.getTasksConfigurator().getPushTask(), dockerBuildImage);

        runSetupImageEnvironmentWithComposeUp();

        assertEquals(DOCKER_IMAGE_ID, composeSettings.getEnvironment().get().get("BUILD_DOCKER_IMAGE_DOCKER_IMAGE"));
    }

    @Test
    public void testFromBuildImageWithEnvironment() {
        DockerBuildImage dockerBuildImage = dockerBuildImageProvider.get();
        composeConvention.fromBuildImage("DOCKER_IMAGE", dockerBuildImage);

        assertTaskDependsOn(composeSettings.getTasksConfigurator().getUpTask(), dockerBuildImage);
        assertTaskDependsOn(composeSettings.getTasksConfigurator().getBuildTask(), dockerBuildImage);
        assertTaskDependsOn(composeSettings.getTasksConfigurator().getPushTask(), dockerBuildImage);

        runSetupImageEnvironmentWithComposeUp();

        assertEquals(DOCKER_IMAGE_ID, composeSettings.getEnvironment().get().get("DOCKER_IMAGE"));
    }

    @Test
    public void testFromBuildImageProvider() {
        composeConvention.fromBuildImage(dockerBuildImageProvider);

        assertTaskDependsOn(composeSettings.getTasksConfigurator().getUpTask(), dockerBuildImageProvider);
        assertTaskDependsOn(composeSettings.getTasksConfigurator().getBuildTask(), dockerBuildImageProvider);
        assertTaskDependsOn(composeSettings.getTasksConfigurator().getPushTask(), dockerBuildImageProvider);

        runSetupImageEnvironmentWithComposeUp();

        assertEquals(DOCKER_IMAGE_ID, composeSettings.getEnvironment().get().get("BUILD_DOCKER_IMAGE_DOCKER_IMAGE"));
    }

    @Test
    public void testFromBuildImageProviderWithEnvironment() {
        composeConvention.fromBuildImage("DOCKER_IMAGE", dockerBuildImageProvider);

        assertTaskDependsOn(composeSettings.getTasksConfigurator().getUpTask(), dockerBuildImageProvider);
        assertTaskDependsOn(composeSettings.getTasksConfigurator().getBuildTask(), dockerBuildImageProvider);
        assertTaskDependsOn(composeSettings.getTasksConfigurator().getPushTask(), dockerBuildImageProvider);

        runSetupImageEnvironmentWithComposeUp();

        assertEquals(DOCKER_IMAGE_ID, composeSettings.getEnvironment().get().get("DOCKER_IMAGE"));
    }

    @Test
    public void testFromProject() {
        composeConvention.fromProject(project);
        TaskCollection<DockerBuildImage> buildImageTaskCollection = project.getTasks().withType(DockerBuildImage.class);

        assertTaskDependsOn(composeSettings.getTasksConfigurator().getUpTask(), dockerBuildImageProvider);
        assertTaskDependsOn(composeSettings.getTasksConfigurator().getBuildTask(), dockerBuildImageProvider);
        assertTaskDependsOn(composeSettings.getTasksConfigurator().getPushTask(), dockerBuildImageProvider);

        runSetupImageEnvironmentWithComposeUp();

        assertEquals(DOCKER_IMAGE_ID, composeSettings.getEnvironment().get().get("BUILD_DOCKER_IMAGE_DOCKER_IMAGE"));
    }

    @Test
    public void testFromOtherProject() {
        Project subProject1 = ProjectBuilder.builder().withParent(project).withName("sub-project1").build();
        Project subProject2 = ProjectBuilder.builder().withParent(project).withName("sub-project2").build();

        TaskProvider<DockerBuildImage> subProject1BuildDockerImageProvider = createBuildDockerImage(subProject1,
                DOCKER_IMAGE_ID);
        TaskProvider<DockerBuildImage> subProject2BuildDockerImageProvider = createBuildDockerImage(subProject2,
                DOCKER_IMAGE_ID);

        composeConvention.fromProject(subProject1);
        composeConvention.fromProject(subProject2);

        assertTaskDependsOn(composeSettings.getTasksConfigurator().getUpTask(), subProject2BuildDockerImageProvider);
        assertTaskDependsOn(composeSettings.getTasksConfigurator().getBuildTask(), subProject2BuildDockerImageProvider);
        assertTaskDependsOn(composeSettings.getTasksConfigurator().getPushTask(), subProject2BuildDockerImageProvider);

        assertTaskDependsOn(composeSettings.getTasksConfigurator().getUpTask(), subProject1BuildDockerImageProvider);
        assertTaskDependsOn(composeSettings.getTasksConfigurator().getBuildTask(), subProject1BuildDockerImageProvider);
        assertTaskDependsOn(composeSettings.getTasksConfigurator().getPushTask(), subProject1BuildDockerImageProvider);

        runSetupImageEnvironmentWithComposeUp();

        assertEquals(DOCKER_IMAGE_ID,
                composeSettings.getEnvironment().get().get("SUB_PROJECT2_BUILD_DOCKER_IMAGE_DOCKER_IMAGE"));
        assertEquals(DOCKER_IMAGE_ID,
                composeSettings.getEnvironment().get().get("SUB_PROJECT1_BUILD_DOCKER_IMAGE_DOCKER_IMAGE"));
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
        runSetupImageEnvironmentWithComposeUp();

        assertEquals(DOCKER_IMAGE_ID, composeSettings.getEnvironment().get().get("SUB_PROJECT1_DOCKER_IMAGE"));
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
        runSetupImageEnvironmentWithComposeUp();

        assertEquals(DOCKER_IMAGE_ID, composeSettings.getEnvironment().get().get("SUB_PROJECT1_DOCKER_IMAGE"));
    }

    @Test
    public void testFromProjectWithEnvironmentVariable() {
        composeConvention.fromProject("TESTNAME", project);
        assertTaskDependsOn(composeSettings.getTasksConfigurator().getUpTask(), dockerBuildImageProvider);
        assertTaskDependsOn(composeSettings.getTasksConfigurator().getBuildTask(), dockerBuildImageProvider);
        assertTaskDependsOn(composeSettings.getTasksConfigurator().getPushTask(), dockerBuildImageProvider);

        runSetupImageEnvironmentWithComposeUp();

        assertEquals(DOCKER_IMAGE_ID, composeSettings.getEnvironment().get().get("TESTNAME_BUILD_DOCKER_IMAGE"));
    }

    @Test
    public void testFromProjectWithEnvironmentVariable1() {
        String DOCKER_IMAGE_ID_2 = "c0ff34";
        TaskProvider<DockerBuildImage> dockerBuildImageProvider = project.getTasks()
                .register("buildDockerImageAlternative", DockerBuildImage.class);
        dockerBuildImageProvider.configure(dockerBuildImage -> {
            dockerBuildImage.getImageId().set(DOCKER_IMAGE_ID_2);
        });
        composeConvention.fromProject("TESTNAME2", project);
        assertTaskDependsOn(composeSettings.getTasksConfigurator().getUpTask(), dockerBuildImageProvider);
        assertTaskDependsOn(composeSettings.getTasksConfigurator().getBuildTask(), dockerBuildImageProvider);
        assertTaskDependsOn(composeSettings.getTasksConfigurator().getPushTask(), dockerBuildImageProvider);

        runSetupImageEnvironmentWithComposeUp();

        assertEquals(DOCKER_IMAGE_ID, composeSettings.getEnvironment().get().get("TESTNAME2_BUILD_DOCKER_IMAGE"));
        assertEquals(DOCKER_IMAGE_ID_2,
                composeSettings.getEnvironment().get().get("TESTNAME2_BUILD_DOCKER_IMAGE_ALTERNATIVE"));
    }

    @Test
    public void testFromProjectWithDockerPluginAndEnvironmentVariable() {
        Project subProject3 = ProjectBuilder.builder().withParent(project).withName("sub-project3").build();
        composeConvention.fromProject("TESTNAME", subProject3);

        subProject3.getProjectDir().mkdirs();

        subProject3.getPluginManager().apply(DockerPlugin.class);
        TaskProvider<DockerBuildImage> subProject1BuildDockerImageProvider = subProject3.getTasks()
                .named("buildDockerImage", DockerBuildImage.class);
        subProject1BuildDockerImageProvider.configure(buildImage -> {
            buildImage.getImageId().set(DOCKER_IMAGE_ID);
        });
        runSetupImageEnvironmentWithComposeUp();

        assertEquals(DOCKER_IMAGE_ID, composeSettings.getEnvironment().get().get("TESTNAME"));
        assertEquals(DOCKER_IMAGE_ID, composeSettings.getEnvironment().get().get("TESTNAME_BUILD_DOCKER_IMAGE"));
    }
}
