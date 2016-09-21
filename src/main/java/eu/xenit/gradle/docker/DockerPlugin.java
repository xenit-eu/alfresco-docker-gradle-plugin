package eu.xenit.gradle.docker;

import com.avast.gradle.dockercompose.ComposeExtension;
import com.avast.gradle.dockercompose.DockerComposePlugin;
import com.bmuschko.gradle.docker.DockerExtension;
import com.bmuschko.gradle.docker.DockerRegistryCredentials;
import com.bmuschko.gradle.docker.DockerRemoteApiPlugin;
import eu.xenit.gradle.git.GitInfoProvider;
import eu.xenit.gradle.git.JGitInfoProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by thijs on 10/24/16.
 * This plugin configures the docker environment.
 */
public class DockerPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        DockerConfig dockerConfig = new DockerConfig(project);
        project.getPluginManager().apply(DockerRemoteApiPlugin.class);
        DockerExtension dockerExtension = (DockerExtension) project.getExtensions().getByName("docker");
        if(dockerConfig.getUrl() != null) {
            dockerExtension.setUrl(dockerConfig.getUrl());
        }

        if (dockerConfig.getCertPath() != null) {
            dockerExtension.setCertPath(new File(dockerConfig.getCertPath()));
        }

        if (dockerConfig.getRegistryUrl() != null) {
            DockerRegistryCredentials registryCredentials = new DockerRegistryCredentials();
            registryCredentials.setUrl(dockerConfig.getRegistryUrl());
            registryCredentials.setUsername(dockerConfig.getRegistryUsername());
            registryCredentials.setPassword(dockerConfig.getRegistryPassword());
            dockerExtension.setRegistryCredentials(registryCredentials);
        }
        GitInfoProvider provider = JGitInfoProvider.GetProviderForProject(project);
        String branch = provider == null ? "no_branch" : provider.getBranch();
        branch = branch == null ? "no_branch" : branch;
        project.getPluginManager().apply(DockerComposePlugin.class);
        ComposeExtension composeExtension = (ComposeExtension) project.getExtensions().getByName("dockerCompose");
        composeExtension.getUseComposeFiles().add("docker-compose.yml");
        composeExtension.getEnvironment().put("DOCKER_HOST", dockerConfig.getUrl());
        composeExtension.getEnvironment().put("DOCKER_IP", dockerConfig.getExposeIp());
        if(dockerConfig.getCertPath()!=null) {

            composeExtension.getEnvironment().put("DOCKER_CERT_PATH", dockerConfig.getCertPath());
            composeExtension.getEnvironment().put("DOCKER_TLS_VERIFY", "0");
        }

        composeExtension.getEnvironment().put("COMPOSE_PROJECT_NAME", project.getName() + "-" + branch);
    }
}
