package eu.xenit.gradle.docker.compose;

import com.avast.gradle.dockercompose.ComposeSettings;
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage;
import eu.xenit.gradle.alfresco.DockerAlfrescoPlugin;
import eu.xenit.gradle.docker.DockerPlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskProvider;

public class DockerComposeConventionImpl implements DockerComposeConvention {

    private final ComposeSettings composeSettings;
    static final String CONFIGURE_COMPOSE_ACTION_NAME = "Configure docker-compose image";

    public DockerComposeConventionImpl(ComposeSettings composeSettings) {
        this.composeSettings = composeSettings;
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

    private void configureBuildImageTask(TaskProvider<DockerBuildImage> buildImageTaskProvider,
            Action<? super DockerBuildImage> action) {
        configureComposeDependencies(buildImageTaskProvider);

        buildImageTaskProvider.configure(dockerBuildImage -> {
            dockerBuildImage.doLast(CONFIGURE_COMPOSE_ACTION_NAME, t -> {
                action.execute(dockerBuildImage);
            });
        });
    }

    private void configureBuildImageTask(DockerBuildImage buildImage, Action<? super DockerBuildImage> action) {
        configureComposeDependencies(buildImage);

        buildImage.doLast(CONFIGURE_COMPOSE_ACTION_NAME, t -> {
            action.execute(buildImage);
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

    public void fromBuildImage(String environmentVariable, TaskProvider<DockerBuildImage> buildImageTaskProvider) {
        configureBuildImageTask(buildImageTaskProvider, createSetComposeEnvironmentAction(environmentVariable));
    }

    public void fromBuildImage(String environmentVariable, DockerBuildImage buildImage) {
        configureBuildImageTask(buildImage, createSetComposeEnvironmentAction(environmentVariable));
    }

    public void fromBuildImage(TaskProvider<DockerBuildImage> buildImageTaskProvider) {
        configureBuildImageTask(buildImageTaskProvider, createSetComposeEnvironmentFromPathAction());
    }

    public void fromBuildImage(DockerBuildImage buildImage) {
        configureBuildImageTask(buildImage, createSetComposeEnvironmentFromPathAction());
    }

    public void fromProject(Project project) {
        TaskCollection<DockerBuildImage> dockerBuildImages = project.getTasks().withType(DockerBuildImage.class);
        configureComposeDependencies(dockerBuildImages);
        dockerBuildImages.configureEach(this::fromBuildImage);

        // Register shortened environment variables for `buildDockerImage` tasks created with the docker or docker-alfresco plugin
        project.getPlugins().withType(DockerPlugin.class, plugin -> {
            String environmentName = Util.safeEnvironmentVariableName(project.getPath().substring(1)) + "_DOCKER_IMAGE";
            fromBuildImage(environmentName, project.getTasks().named("buildDockerImage", DockerBuildImage.class));
        });
        project.getPlugins().withType(DockerAlfrescoPlugin.class, plugin -> {
            String environmentName = Util.safeEnvironmentVariableName(project.getPath().substring(1)) + "_DOCKER_IMAGE";
            fromBuildImage(environmentName, project.getTasks().named("buildDockerImage", DockerBuildImage.class));
        });
    }

    public void fromProject(String projectName) {
        fromProject(composeSettings.getProject().project(projectName));
    }
}
