package eu.xenit.gradle.docker;

import static eu.xenit.gradle.git.JGitInfoProvider.GetProviderForProject;

import com.avast.gradle.dockercompose.ComposeExtension;
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage;
import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import eu.xenit.gradle.JenkinsUtil;
import eu.xenit.gradle.docker.tasks.internal.DeprecatedTask;
import eu.xenit.gradle.docker.tasks.internal.DockerBuildImage;
import eu.xenit.gradle.git.CannotConvertToUrlException;
import eu.xenit.gradle.git.GitInfoProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

/**
 * Created by thijs on 10/25/16.
 */
public class DockerBuildBehavior {

    private static final Logger LOGGER = Logging.getLogger(DockerBuildBehavior.class);

    private Supplier<DockerBuildExtension> dockerBuildExtension;
    private Supplier<File> dockerFile;
    private Dockerfile dockerfileCreator;

    public DockerBuildBehavior(Supplier<DockerBuildExtension> dockerBuildExtension, Supplier<File> dockerFile) {
        this.dockerBuildExtension = dockerBuildExtension;
        this.dockerFile = dockerFile;
    }

    public DockerBuildBehavior(Supplier<DockerBuildExtension> dockerBuildExtension, Dockerfile dockerfileCreator) {
        this.dockerBuildExtension = dockerBuildExtension;
        this.dockerfileCreator = dockerfileCreator;
    }

    public void apply(Project project) {
        this.execute(project);
    }

    public void execute(Project project) {
        DockerBuildImage buildDockerImage = createDockerBuildImageTask(project, dockerBuildExtension);
        buildDockerImage.setDescription("Build the docker image");
        if (dockerfileCreator != null) {
            buildDockerImage.getDockerFile().set(dockerfileCreator.getDestFile());
            buildDockerImage.dependsOn(dockerfileCreator);
        }

        if (dockerFile != null) {
            buildDockerImage.getDockerFile().set(dockerFile::get);
        }

        buildDockerImage.getInputDir().set(project.provider(() -> {
            DirectoryProperty directoryProperty = project.getObjects().directoryProperty();
            directoryProperty.set(buildDockerImage.getDockerFile().getAsFile().get().getParentFile());
            return directoryProperty.get();
        }));

        buildDockerImage.getLabels().set(project.provider(() -> this.getLabels(project)));
        buildDockerImage.getTags().set(project
                .provider(() -> this.getTags().stream().map(tag -> getDockerRepository() + ":" + tag).collect(
                        Collectors.toSet())));

        buildDockerImage.doLast(task -> {
            ComposeExtension composeExtension = (ComposeExtension) project.getExtensions().getByName("dockerCompose");
            composeExtension.getEnvironment().put("DOCKER_IMAGE", buildDockerImage.getImageId().get());
        });

        project.getTasks().create("labelDockerFile", DeprecatedTask.class).setReplacementTask(buildDockerImage);
        project.getTasks().create("buildLabels", DeprecatedTask.class).setReplacementTask(buildDockerImage);

        DefaultTask dockerPushImage = project.getTasks().create("pushDockerImage", DefaultTask.class);
        dockerPushImage.setGroup("Docker");
        dockerPushImage.setDescription("Collection of all the pushTags");

        project.afterEvaluate((project1 -> {
            List<DockerPushImage> pushTags = getPushTags(project, buildDockerImage);
            dockerPushImage.dependsOn(pushTags);
        }));

        Task task = project.getTasks().getAt("composeUp");
        task.dependsOn(buildDockerImage);
    }

    private DockerBuildImage createDockerBuildImageTask(Project project,
            Supplier<DockerBuildExtension> dockerBuildExtension) {
        DockerBuildImage dockerBuildImage = (DockerBuildImage) project.getTasks().findByName("buildDockerImage");
        if (dockerBuildImage == null) {
            dockerBuildImage = project.getTasks().create("buildDockerImage", DockerBuildImage.class);
        }

        DockerBuildImage finalDockerBuildImage = dockerBuildImage;
        project.afterEvaluate((project1) -> {
            DockerBuildExtension extension = dockerBuildExtension.get();
            finalDockerBuildImage.getPull().set(extension.getPull());
            finalDockerBuildImage.getNoCache().set(extension.getNoCache());
            finalDockerBuildImage.getRemove().set(extension.getRemove());
        });

        return dockerBuildImage;
    }

    private Map<String, String> getLabels(Project project) {
        Map<String, String> labels = new HashMap<>();
        GitInfoProvider gitInfoProvider = GetProviderForProject(project);
        String labelPrefix = "eu.xenit.gradle-plugin.git.";
        if (gitInfoProvider != null) {
            if (gitInfoProvider.getOrigin() != null) {
                labels.put(labelPrefix + "origin", gitInfoProvider.getOrigin());
                try {
                    labels.put(labelPrefix + "commit.url", gitInfoProvider.getCommitURL().toExternalForm());
                } catch (CannotConvertToUrlException e) {
                    LOGGER.info("Cannot create commit url");
                    LOGGER.debug("Stacktrace for the above info", e);
                }
            }
            labels.put(labelPrefix + "branch", gitInfoProvider.getBranch());
            labels.put(labelPrefix + "commit.id", gitInfoProvider.getCommitChecksum());
            labels.put(labelPrefix + "commit.author", gitInfoProvider.getCommitAuthor());
            labels.put(labelPrefix + "commit.message", '"' + gitInfoProvider.getCommitMessage()
                    .replaceAll("\"", "\\\\\"")
                    .replaceAll("(\r)*\n", "\\\\\n") + '"');
        }
        return labels;
    }

    private Set<String> getTags() {
        List<String> tags = dockerBuildExtension.get().getTags();
        boolean automaticTags = dockerBuildExtension.get().getAutomaticTags();

        if (automaticTags) {
            tags = tags.stream().map(tag -> {
                if (isMaster()) {
                    return tag;
                } else {
                    return JenkinsUtil.getBranch() + "-" + tag;
                }
            }).collect(Collectors.toList());

            if (JenkinsUtil.getBuildId() != null) {
                if (isMaster()) {
                    tags.add("build-" + JenkinsUtil.getBuildId());
                } else {
                    tags.add(JenkinsUtil.getBranch() + "-build-" + JenkinsUtil.getBuildId());
                }
            }

            if (isMaster()) {
                tags.add("latest");
            } else {
                tags.add(JenkinsUtil.getBranch());
            }
        }

        return new HashSet<>(tags);
    }

    private boolean isMaster() {
        return "master".equals(JenkinsUtil.getBranch());
    }

    private List<DockerPushImage> getPushTags(Project project, DockerBuildImage dockerBuildImage) {
        List<DockerPushImage> result = new ArrayList<>();
        for (String tag : this.getTags()) {
            DockerPushImage pushTag = project.getTasks().create("pushTag" + tag, DockerPushImage.class);
            pushTag.getImageName().set(getDockerRepository());
            pushTag.getTag().set(tag);
            pushTag.dependsOn(dockerBuildImage);
            pushTag.setDescription("Push image with tag " + tag);
            result.add(pushTag);
        }
        return result;
    }

    private String getDockerRepository() {
        return dockerBuildExtension.get().getRepository();
    }

}
