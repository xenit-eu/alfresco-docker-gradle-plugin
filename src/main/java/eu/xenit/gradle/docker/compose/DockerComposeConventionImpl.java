package eu.xenit.gradle.docker.compose;

import com.avast.gradle.dockercompose.ComposeSettings;
import com.bmuschko.gradle.docker.DockerRemoteApiPlugin;
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage;
import eu.xenit.gradle.docker.DockerPlugin;
import eu.xenit.gradle.docker.alfresco.DockerAlfrescoPlugin;
import java.util.function.Supplier;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskProvider;

class DockerComposeConventionImpl implements DockerComposeConvention {

    private final ComposeSettings composeSettings;
    private final PluginClasspathChecker pluginClasspathChecker;
    static final String CONFIGURE_COMPOSE_ACTION_NAME = "Configure docker-compose image id from DockerBuildImage task";

    DockerComposeConventionImpl(ComposeSettings composeSettings) {
        this.composeSettings = composeSettings;
        this.pluginClasspathChecker = new PluginClasspathChecker(composeSettings.getProject());
    }

    private void configureComposeDependencies(Object dependencies) {
        configureComposeTasks(composeTask -> {
            composeTask.dependsOn(dependencies);
        });
    }

    private void configureComposeTasks(Action<? super Task> action) {
        composeSettings.getUpTask().configure(action);
        composeSettings.getBuildTask().configure(action);
        composeSettings.getPushTask().configure(action);
    }

    private void configureComposeEnvironment(Action<? super DockerBuildImage> action,
            Supplier<DockerBuildImage> dockerBuildImageSupplier) {
        configureComposeTasks(composeTask -> {
            composeTask.doFirst(CONFIGURE_COMPOSE_ACTION_NAME, new Action<Task>() { // No lambda -> see #96
                @Override
                public void execute(Task t) {
                    action.execute(dockerBuildImageSupplier.get());
                }
            });
        });
    }

    private Action<? super DockerBuildImage> createSetComposeEnvironmentAction(String environmentVariable) {
        return dockerBuildImage -> {
            composeSettings.getEnvironment().put(environmentVariable, dockerBuildImage.getImageId().get());
        };
    }

    private Action<? super DockerBuildImage> createSetComposeEnvironmentFromPathAction() {
        return dockerBuildImage -> {
            String environmentVariable =
                    Util.safeEnvironmentVariableName(dockerBuildImage.getPath().substring(1)) + "_DOCKER_IMAGE";
            createSetComposeEnvironmentAction(environmentVariable).execute(dockerBuildImage);
        };
    }

    @Override
    public void fromBuildImage(String environmentVariable,
            TaskProvider<? extends DockerBuildImage> buildImageTaskProvider) {
        configureComposeDependencies(buildImageTaskProvider);
        configureComposeEnvironment(createSetComposeEnvironmentAction(environmentVariable), buildImageTaskProvider::get);
    }

    @Override
    public void fromBuildImage(String environmentVariable, DockerBuildImage buildImage) {
        configureComposeDependencies(buildImage);
        configureComposeEnvironment(createSetComposeEnvironmentAction(environmentVariable), () -> buildImage);
    }

    @Override
    public void fromBuildImage(TaskProvider<? extends DockerBuildImage> buildImageTaskProvider) {
        configureComposeDependencies(buildImageTaskProvider);
        configureComposeEnvironment(createSetComposeEnvironmentFromPathAction(), buildImageTaskProvider::get);
    }

    @Override
    public void fromBuildImage(DockerBuildImage buildImage) {
        configureComposeDependencies(buildImage);
        configureComposeEnvironment(createSetComposeEnvironmentFromPathAction(), () -> buildImage);
    }

    public void fromBuildImage(Task buildImage) {
        fromBuildImage(pluginClasspathChecker.checkTask(DockerBuildImage.class, buildImage));
    }

    @Override
    public void fromProject(Project project) {
        TaskCollection<DockerBuildImage> dockerBuildImages = project.getTasks().withType(DockerBuildImage.class);
        configureComposeDependencies(dockerBuildImages);
        configureComposeTasks(composeTask -> {
            dockerBuildImages.forEach(buildImage -> {
                DockerBuildImage dockerBuildImage = pluginClasspathChecker
                        .checkTask(DockerBuildImage.class, buildImage);
                composeTask.doFirst(CONFIGURE_COMPOSE_ACTION_NAME, new Action<Task>() { // No lambda -> see #96
                    @Override
                    public void execute(Task t) {
                        createSetComposeEnvironmentFromPathAction().execute(dockerBuildImage);
                    }
                });
            });
        });

        // Register shortened environment variables for `buildDockerImage` tasks created with the docker or docker-alfresco plugin
        pluginClasspathChecker.withPlugin(project, DockerPlugin.class, DockerPlugin.PLUGIN_ID, plugin -> {
            String environmentName = Util.safeEnvironmentVariableName(project.getPath().substring(1)) + "_DOCKER_IMAGE";
            fromBuildImage(environmentName, project.getTasks().named("buildDockerImage", DockerBuildImage.class));
        });
        pluginClasspathChecker
                .withPlugin(project, DockerAlfrescoPlugin.class, DockerAlfrescoPlugin.PLUGIN_ID, plugin -> {
                    String environmentName =
                            Util.safeEnvironmentVariableName(project.getPath().substring(1)) + "_DOCKER_IMAGE";
                    fromBuildImage(environmentName,
                            project.getTasks().named("buildDockerImage", DockerBuildImage.class));
                });
        // Check plugin classpath for docker remote api plugin
        pluginClasspathChecker.checkPlugin(project, DockerRemoteApiPlugin.class, "com.bmuschko.docker-remote-api");
    }

    @Override
    public void fromProject(String projectName) {
        fromProject(composeSettings.getProject().project(projectName));
    }
}
