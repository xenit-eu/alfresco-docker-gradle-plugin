package eu.xenit.gradle.docker.config;

import java.net.URI;
import java.net.URISyntaxException;
import org.gradle.api.Project;

/**
 * Created by thijs on 10/24/16.
 */
public class DockerConfig {

    public static final String EU_XENIT_DOCKER_URL = "eu.xenit.docker.url";
    public static final String EU_XENIT_DOCKER_EXPOSE_IP = "eu.xenit.docker.expose.ip";
    public static final String EU_XENIT_DOCKER_CERT_PATH = "eu.xenit.docker.certPath";
    public static final String EU_XENIT_DOCKER_REGISTRY_URL = "eu.xenit.docker.registry.url";
    public static final String EU_XENIT_DOCKER_REGISTRY_USERNAME = "eu.xenit.docker.registry.username";
    public static final String EU_XENIT_DOCKER_REGISTRY_PASSWORD = "eu.xenit.docker.registry.password";

    public String getUrl() {
        return url;
    }

    public String getExposeIp() {
        return exposeIp;
    }

    public String getCertPath() {
        return certPath;
    }

    public String getRegistryUrl() {
        return registryUrl;
    }

    public String getRegistryUsername() {
        return registryUsername;
    }

    public String getRegistryPassword() {
        return registryPassword;
    }

    private String url;
    private String exposeIp;
    private String certPath;
    private String registryUrl;
    private String registryUsername;
    private String registryPassword;

    public DockerConfig(Project project) {
        this.url = (String) project.getProperties().get(EU_XENIT_DOCKER_URL);

        if (project.getProperties().containsKey(EU_XENIT_DOCKER_EXPOSE_IP)) {
            exposeIp = (String) project.getProperties().get(EU_XENIT_DOCKER_EXPOSE_IP);
        } else if (this.url != null) {
            // Try to extract ip to expose from the docker url
            try {
                exposeIp = (new URI(this.url)).getHost();
            } catch (URISyntaxException e) {

            }
        }
        // If that did not work, fall back to localhost
        if (exposeIp == null) {
            exposeIp = "127.0.0.1";
        }

        if (project.getProperties().containsKey(EU_XENIT_DOCKER_CERT_PATH)) {
            certPath = (String) project.getProperties().get(EU_XENIT_DOCKER_CERT_PATH);
        }
        if (project.hasProperty(EU_XENIT_DOCKER_REGISTRY_URL)) {
            this.registryUrl = (String) project.getProperties().get(EU_XENIT_DOCKER_REGISTRY_URL);
        }
        if (project.hasProperty(EU_XENIT_DOCKER_REGISTRY_USERNAME)) {
            this.registryUsername = (String) project.getProperties().get(EU_XENIT_DOCKER_REGISTRY_USERNAME);
        }
        if (project.hasProperty(EU_XENIT_DOCKER_REGISTRY_PASSWORD)) {
            this.registryPassword = (String) project.getProperties().get(EU_XENIT_DOCKER_REGISTRY_PASSWORD);
        }
    }

}
