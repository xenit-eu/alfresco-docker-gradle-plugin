package eu.xenit.gradle.docker.compose;

import com.avast.gradle.dockercompose.ComposeSettings;
import com.bmuschko.gradle.docker.DockerRemoteApiPlugin;
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage;
import eu.xenit.gradle.docker.alfresco.DockerAlfrescoPlugin;
import eu.xenit.gradle.docker.DockerPlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskProvider;

public class DockerComposeConventionImpl implements DockerComposeConvention {

    private final ComposeSettings composeSettings;
    private final PluginClasspathChecker pluginClasspathChecker;
    static final String CONFIGURE_COMPOSE_ACTION_NAME = "Configure docker-compose image";

    public DockerComposeConventionImpl(ComposeSettings composeSettings) {
        this.composeSettings = composeSettings;
        this.pluginClasspathChecker = new PluginClasspathChecker(composeSettings.getProject());
    }

    private void configureComposeDependencies(Object dependencies) {
        composeSettings.getUpTask().configure(composeUp -> {
            composeUp.dependsOn(dependencies);
        });
        composeSettings.getBuildTask().configure(composeBuild -> {
            composeBuild.dependsOn(dependencies);
        });
        composeSettings.getPushTask().configure(composePush -> {
            composePush.dependsOn(dependencies);
        });
    }

    private void configureBuildImageTask(TaskProvider<? extends Task> buildImageTaskProvider,
            Action<? super DockerBuildImage> action) {
        buildImageTaskProvider.configure(dockerBuildImage -> {
            configureBuildImageTask(dockerBuildImage, action);
        });
    }

    private void configureBuildImageTask(Task buildImage, Action<? super DockerBuildImage> action) {
        DockerBuildImage dockerBuildImage = pluginClasspathChecker.checkTask(DockerBuildImage.class, buildImage);
        dockerBuildImage.doLast(CONFIGURE_COMPOSE_ACTION_NAME, t -> {
            action.execute(dockerBuildImage);
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
    public void fromBuildImage(String environmentVariable, TaskProvider<? extends DockerBuildImage> buildImageTaskProvider) {
        configureComposeDependencies(buildImageTaskProvider);
        configureBuildImageTask(buildImageTaskProvider, createSetComposeEnvironmentAction(environmentVariable));
    }

    @Override
    public void fromBuildImage(String environmentVariable, DockerBuildImage buildImage) {
        configureComposeDependencies(buildImage);
        configureBuildImageTask(buildImage, createSetComposeEnvironmentAction(environmentVariable));
    }

    @Override
    public void fromBuildImage(TaskProvider<? extends DockerBuildImage> buildImageTaskProvider) {
        configureComposeDependencies(buildImageTaskProvider);
        configureComposeDependencies(buildImageTaskProvider);
        configureBuildImageTask(buildImageTaskProvider, createSetComposeEnvironmentFromPathAction());
    }

    @Override
    public void fromBuildImage(DockerBuildImage buildImage) {
        configureComposeDependencies(buildImage);
        configureBuildImageTask(buildImage, createSetComposeEnvironmentFromPathAction());
    }

    public void fromBuildImage(Task buildImage) {
        fromBuildImage(pluginClasspathChecker.checkTask(DockerBuildImage.class, buildImage));
    }

    private void fromProjectBuildImage(Task buildImage) {
        configureBuildImageTask(buildImage, createSetComposeEnvironmentFromPathAction());
    }

    @Override
    public void fromProject(Project project) {
        TaskCollection<DockerBuildImage> dockerBuildImages = project.getTasks().withType(DockerBuildImage.class);
        configureComposeDependencies(dockerBuildImages);
        dockerBuildImages.configureEach(this::fromProjectBuildImage);

        // Register shortened environment variables for `buildDockerImage` tasks created with the docker or docker-alfresco plugin
        pluginClasspathChecker.withPlugin(project, DockerPlugin.class, DockerPlugin.PLUGIN_ID, plugin -> {
            String environmentName = Util.safeEnvironmentVariableName(project.getPath().substring(1)) + "_DOCKER_IMAGE";
            fromBuildImage(environmentName, project.getTasks().named("buildDockerImage", DockerBuildImage.class));
        });
        pluginClasspathChecker.withPlugin(project, DockerAlfrescoPlugin.class, DockerAlfrescoPlugin.PLUGIN_ID, plugin -> {
            String environmentName = Util.safeEnvironmentVariableName(project.getPath().substring(1)) + "_DOCKER_IMAGE";
            fromBuildImage(environmentName, project.getTasks().named("buildDockerImage", DockerBuildImage.class));
        });
        // Check plugin classpath for docker remote api plugin
        pluginClasspathChecker.checkPlugin(project, DockerRemoteApiPlugin.class, "com.bmuschko.docker-remote-api");
    }

    @Override
    public void fromProject(String projectName) {
        fromProject(composeSettings.getProject().project(projectName));
    }
}
