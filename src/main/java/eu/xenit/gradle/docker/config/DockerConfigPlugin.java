package eu.xenit.gradle.docker.config;

import com.avast.gradle.dockercompose.ComposeSettings;
import com.bmuschko.gradle.docker.DockerExtension;
import com.bmuschko.gradle.docker.DockerRemoteApiPlugin;
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage;
import eu.xenit.gradle.docker.compose.DockerComposePlugin;
import eu.xenit.gradle.docker.internal.Deprecation;
import java.io.File;
import java.util.UUID;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.util.GradleVersion;

/**
 * Created by thijs on 10/24/16.
 * This plugin configures the docker environment.
 */
public class DockerConfigPlugin implements Plugin<Project> {

    private static final Logger LOGGER = Logging.getLogger(DockerConfigPlugin.class);

    public static final String PLUGIN_ID = "eu.xenit.docker-config";

    @Override
    public void apply(Project project) {
        if (GradleVersion.current().compareTo(GradleVersion.version("5.2")) < 0) {
            throw new GradleException(
                    "The "+PLUGIN_ID+" plugin requires at least Gradle 5.2. You are running "
                            + GradleVersion.current());
        }
        // Set up deprecation warnings
        Deprecation.setStartParameter(project.getGradle().getStartParameter());
        project.getGradle().projectsEvaluated(g -> {
            g.buildFinished(buildResult -> {
                Deprecation.printSummary();
            });
        });

        // Rest of the configuration
        DockerConfig dockerConfig = new DockerConfig(project);
        project.getPluginManager().apply(DockerRemoteApiPlugin.class);
        DockerExtension dockerExtension = project.getExtensions().getByType(DockerExtension.class);
        if (dockerConfig.getUrl() != null) {
            dockerExtension.getUrl().set(dockerConfig.getUrl());
        }

        if (dockerConfig.getCertPath() != null) {
            dockerExtension.getCertPath().set(new File(dockerConfig.getCertPath()));
        }

        if (dockerConfig.getRegistryUrl() != null) {
            dockerExtension.registryCredentials(registryCredentials -> {
                registryCredentials.getUrl().set(dockerConfig.getRegistryUrl());
                registryCredentials.getUsername().set(dockerConfig.getRegistryUsername());
                registryCredentials.getPassword().set(dockerConfig.getRegistryPassword());
            });
        }

        project.getPlugins().withType(DockerComposePlugin.class, dockerComposePlugin -> {
            ComposeSettings composeExtension = dockerComposePlugin.getComposeSettings();
            composeExtension.getUseComposeFiles().add("docker-compose.yml");
            composeExtension.getEnvironment().put("DOCKER_HOST", dockerExtension.getUrl().get());
            composeExtension.getEnvironment().put("SERVICES_HOST", dockerConfig.getExposeIp());
            composeExtension.getEnvironment().put("DOCKER_IP", dockerConfig.getExposeIp());
            if (dockerConfig.getCertPath() != null) {
                composeExtension.getEnvironment().put("DOCKER_CERT_PATH", dockerConfig.getCertPath());
            }
        });

        project.getPlugins().withType(com.avast.gradle.dockercompose.DockerComposePlugin.class, dockerComposePlugin -> {
            throw new GradleException(
                    "The com.avast.gradle.docker-compose plugin does not work together with "+PLUGIN_ID+".\n"+
                            "Use "+DockerComposePlugin.PLUGIN_ID+" for docker-compose functionality."
            );
        });

        if(project.getGradle().getStartParameter().isOffline()) {
            // We need to create a separate task to disable pull on DockerBuildImage tasks, because in a doFirst block, providers are already finalized.
            TaskProvider<DefaultTask> disableDockerBuildImagePull = project.getTasks().register("_"+PLUGIN_ID+"_disableDockerBuildImagePull_"+ UUID.randomUUID(), DefaultTask.class, task -> {
                task.setDescription("["+PLUGIN_ID+" internal] Disables pull flag on DockerBuildImage tasks when Gradle runs in offline mode.");
                task.doLast(new Action<Task>() {
                    @Override
                    public void execute(Task task) {
                        project.getTasks().withType(DockerBuildImage.class).configureEach(dockerBuildImage -> {
                            if(dockerBuildImage.getPull().get()) {
                                dockerBuildImage.doFirst(new Action<Task>() {
                                    @Override
                                    public void execute(Task task) {
                                        LOGGER.warn("Gradle is running in offline mode, automatic pull has been disabled.");
                                    }
                                });
                                dockerBuildImage.getPull().set(false);
                            }
                        });
                    }
                });
            });
            project.getTasks().withType(DockerBuildImage.class).configureEach(dockerBuildImage -> {
                dockerBuildImage.dependsOn(disableDockerBuildImagePull);
            });
        }

    }
}
