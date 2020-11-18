package eu.xenit.gradle.docker.core;

import com.bmuschko.gradle.docker.tasks.image.DockerPushImage;
import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import eu.xenit.gradle.docker.DockerLegacyPlugin;
import eu.xenit.gradle.docker.label.DockerLabelExtension;
import eu.xenit.gradle.docker.label.DockerLabelPlugin;
import eu.xenit.gradle.docker.autotag.DockerAutotagPlugin;
import eu.xenit.gradle.docker.compose.DockerComposePlugin;
import eu.xenit.gradle.docker.config.DockerConfigPlugin;
import eu.xenit.gradle.docker.tasks.DockerfileWithCopyTask;
import eu.xenit.gradle.docker.tasks.internal.ConsolidateFileCopyInstructionsAction;
import eu.xenit.gradle.docker.tasks.internal.DockerBuildImage;
import eu.xenit.gradle.docker.tasks.internal.Workaround7ConsecutiveCopyDockerBugAction;
import java.util.Optional;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.tasks.TaskProvider;

public class DockerPlugin implements Plugin<Project> {

    public static final String PLUGIN_ID = "eu.xenit.docker";
    private static final String TASK_GROUP = "Docker";
    private DockerExtension dockerExtension;

    public DockerExtension getExtension() {
        return dockerExtension;
    }

    private static boolean readPropertyFlag(Project project, String property) {
        return Optional.ofNullable(project.findProperty(property))
                .map(Object::toString)
                .map(Boolean::parseBoolean)
                .orElse(false);
    }

    @Override
    public void apply(Project project) {
        dockerExtension = project.getExtensions().create("dockerBuild", DockerExtension.class);

        project.getPluginManager().apply(DockerConfigPlugin.class);
        project.getPluginManager().apply(DockerAutotagPlugin.class);
        project.getPluginManager().apply(DockerLabelPlugin.class);

        project.getPluginManager().apply(DockerLegacyPlugin.class);

        // Configure labeling from git
        dockerExtension.getExtensions().getByType(DockerLabelExtension.class).fromGit();

        // Configure an empty createDockerFile task that can be configured by the user.
        // If no Dockerfile is present in the project, the Dockerfile created by this task
        // will be used by buildDockerImage
        TaskProvider<? extends Dockerfile> dockerfileProvider = project.getTasks()
                .register("createDockerFile", DockerfileWithCopyTask.class, dockerfile -> {
                    dockerfile.setDescription("Create a Dockerfile");
                    dockerfile.setGroup(TASK_GROUP);
                    if (readPropertyFlag(project, Workaround7ConsecutiveCopyDockerBugAction.FEATURE_FLAG)) {
                        dockerfile.doFirst("Mitigate Docker COPY bug", new Workaround7ConsecutiveCopyDockerBugAction());
                    }
                    dockerfile.doFirst("Consolidate COPY instructions", new ConsolidateFileCopyInstructionsAction());
                });
        dockerExtension.getDockerFile().convention(dockerfileProvider.flatMap(Dockerfile::getDestFile));

        // Fallback to using the Dockerfile in the root of the project if it exists
        RegularFile detectedDockerFile = project.getLayout().getProjectDirectory().file("Dockerfile");
        if (detectedDockerFile.getAsFile().exists()) {
            dockerExtension.getDockerFile().convention(detectedDockerFile);
        }

        TaskProvider<? extends DockerBuildImage> buildImageProvider = project.getTasks()
                .register("buildDockerImage", DockerBuildImage.class, buildImage -> {
                    buildImage.setDescription("Build the docker image");
                    buildImage.setGroup(TASK_GROUP);
                    buildImage.getPull().set(true);
                    buildImage.getRemove().set(true);

                    buildImage.getImages().set(dockerExtension.getImages());
                    buildImage.getDockerFile().set(dockerExtension.getDockerFile());
                    buildImage.getInputDir().set(buildImage.getDockerFile().flatMap(dockerFile -> {
                        DirectoryProperty directoryProperty = project.getObjects().directoryProperty();
                        directoryProperty.set(dockerFile.getAsFile().getParentFile());
                        return directoryProperty;
                    }));
                });

        project.getTasks().register("pushDockerImage", DockerPushImage.class, dockerPushImage -> {
            dockerPushImage.setDescription("Push all docker image tags");
            dockerPushImage.setGroup(TASK_GROUP);
            dockerPushImage.getImages().set(buildImageProvider.flatMap(DockerBuildImage::getImages));
            dockerPushImage.dependsOn(buildImageProvider);
        });

        project.getPlugins().withType(DockerComposePlugin.class, dockerComposePlugin -> {
            dockerComposePlugin.getDockerComposeConvention().fromBuildImage("DOCKER_IMAGE", buildImageProvider);
        });
    }
}
