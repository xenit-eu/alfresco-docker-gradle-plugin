package eu.xenit.gradle.docker;

import com.avast.gradle.dockercompose.ComposeExtension;
import com.avast.gradle.dockercompose.DockerComposePlugin;
import com.bmuschko.gradle.docker.DockerExtension;
import com.bmuschko.gradle.docker.DockerRegistryCredentials;
import com.bmuschko.gradle.docker.DockerRemoteApiPlugin;
import eu.xenit.gradle.docker.internal.Deprecation;
import java.io.File;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.util.GradleVersion;

/**
 * Created by thijs on 10/24/16.
 * This plugin configures the docker environment.
 */
public class DockerConfigPlugin implements Plugin<Project> {

    public static final String PLUGIN_ID = "eu.xenit.docker-config";

    @Override
    public void apply(Project project) {
        if (GradleVersion.current().compareTo(GradleVersion.version("5.2")) < 0) {
            throw new GradleException(
                    "The  eu.xenit.docker-config plugin requires at least Gradle 5.2. You are running "
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
        DockerExtension dockerExtension = (DockerExtension) project.getExtensions().getByName("docker");
        if (dockerConfig.getUrl() != null) {
            dockerExtension.getUrl().set(dockerConfig.getUrl());
        }

        if (dockerConfig.getCertPath() != null) {
            dockerExtension.getCertPath().set(new File(dockerConfig.getCertPath()));
        }

        if (dockerConfig.getRegistryUrl() != null) {
            DockerRegistryCredentials registryCredentials = (DockerRegistryCredentials) ((ExtensionAware) dockerExtension)
                    .getExtensions().getByName("registryCredentials");
            registryCredentials.getUrl().set(dockerConfig.getRegistryUrl());
            registryCredentials.getUsername().set(dockerConfig.getRegistryUsername());
            registryCredentials.getPassword().set(dockerConfig.getRegistryPassword());
        }

        project.getPlugins().withType(DockerComposePlugin.class, dockerComposePlugin -> {
            ComposeExtension composeExtension = (ComposeExtension) project.getExtensions().getByName("dockerCompose");
            composeExtension.getUseComposeFiles().add("docker-compose.yml");
            composeExtension.getEnvironment().put("DOCKER_HOST", dockerExtension.getUrl().get());
            composeExtension.getEnvironment().put("SERVICES_HOST", dockerConfig.getExposeIp());
            composeExtension.getEnvironment().put("DOCKER_IP", dockerConfig.getExposeIp());
            if (dockerConfig.getCertPath() != null) {
                composeExtension.getEnvironment().put("DOCKER_CERT_PATH", dockerConfig.getCertPath());
            }
        });

    }
}
