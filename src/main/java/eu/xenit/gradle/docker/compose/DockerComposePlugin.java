package eu.xenit.gradle.docker.compose;

import com.avast.gradle.dockercompose.ComposeSettings;
import eu.xenit.gradle.docker.config.DockerConfigPlugin;
import eu.xenit.gradle.docker.internal.GradleVersionRequirement;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class DockerComposePlugin implements Plugin<Project> {

    public static final String PLUGIN_ID = "eu.xenit.docker-compose";

    private DockerComposeExtensionOverride dockerComposeConvention;

    @Override
    public void apply(Project project) {
        GradleVersionRequirement.atLeast("7.0", String.format("use the %s plugin", PLUGIN_ID));
        dockerComposeConvention = project.getExtensions()
                .create("dockerCompose", DockerComposeExtensionOverride.class, project);
        project.getPluginManager().apply(DockerConfigPlugin.class);
    }

    public DockerComposeConvention getDockerComposeConvention() {
        return dockerComposeConvention;
    }

    public ComposeSettings getComposeSettings() {
        return dockerComposeConvention;
    }
}
