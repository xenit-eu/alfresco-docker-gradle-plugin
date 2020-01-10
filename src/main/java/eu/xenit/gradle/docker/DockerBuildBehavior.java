package eu.xenit.gradle.docker;

import static eu.xenit.gradle.docker.internal.git.JGitInfoProvider.GetProviderForProject;

import com.avast.gradle.dockercompose.ComposeExtension;
import com.avast.gradle.dockercompose.DockerComposePlugin;
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage;
import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import eu.xenit.gradle.JenkinsUtil;
import eu.xenit.gradle.docker.tasks.internal.DockerBuildImage;
import eu.xenit.gradle.docker.internal.git.CannotConvertToUrlException;
import eu.xenit.gradle.docker.internal.git.GitInfoProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

/**
 * Created by thijs on 10/25/16.
 */
public class DockerBuildBehavior {

    private static final Logger LOGGER = Logging.getLogger(DockerBuildBehavior.class);
    private static final int DOCKER_TAG_LENGTH_CONSTRAINT = 128;
    private static final String DOCKER_TAG_LENGTH_CONSTRAINT_ERRORMSG = "Automatic tags will violate tag length constraint of "+DOCKER_TAG_LENGTH_CONSTRAINT+", due to usage of branch name in tag. Modify branch name or disable automatic tags.";

    private DockerBuildExtension dockerBuildExtension;
    private RegularFileProperty dockerFile;
    private Dockerfile dockerfileCreator;

    public DockerBuildBehavior(DockerBuildExtension dockerBuildExtension, RegularFileProperty dockerFile) {
        this.dockerBuildExtension = dockerBuildExtension;
        this.dockerFile = dockerFile;
    }

    public DockerBuildBehavior(DockerBuildExtension dockerBuildExtension, Dockerfile dockerfileCreator) {
        this.dockerBuildExtension = dockerBuildExtension;
        this.dockerfileCreator = dockerfileCreator;
    }

    public void apply(Project project) {
        this.execute(project);
    }

    public void execute(Project project) {
        TaskProvider<DockerBuildImage> buildDockerImageProvider = createDockerBuildImageTask(project,
                buildDockerImage -> {
                    buildDockerImage.setDescription("Build the docker image");
                    if (dockerfileCreator != null) {
                        buildDockerImage.getDockerFile().set(dockerfileCreator.getDestFile());
                        buildDockerImage.dependsOn(dockerfileCreator);
                    }

                    if (dockerFile != null) {
                        buildDockerImage.getDockerFile().set(dockerFile);
                    }

                    buildDockerImage.getInputDir().set(project.provider(() -> {
                        DirectoryProperty directoryProperty = project.getObjects().directoryProperty();
                        directoryProperty.set(buildDockerImage.getDockerFile().getAsFile().get().getParentFile());
                        return directoryProperty.get();
                    }));

                    buildDockerImage.getLabels().set(project.provider(() -> this.getLabels(project)));
                    buildDockerImage.getImages().set(
                            getTags()
                                    .map(tags -> tags.stream()
                                            .map(tag -> dockerBuildExtension.getRepository().get() + ":" + tag)
                                            .collect(Collectors.toSet())
                                    )
                    );
                });

        project.getTasks().register("pushDockerImage", DockerPushImage.class, dockerPushImage -> {
            dockerPushImage.setGroup("Docker");
            dockerPushImage.setDescription("Collection of all the pushTags");
            dockerPushImage.getImages().set(buildDockerImageProvider.flatMap(DockerBuildImage::getImages));

        });

        project.getPlugins().withType(DockerComposePlugin.class, dockerComposePlugin -> {
            buildDockerImageProvider.configure(dockerBuildImage -> {
                dockerBuildImage.doLast(t -> {
                    ComposeExtension composeExtension = (ComposeExtension) project.getExtensions()
                            .getByName("dockerCompose");
                    composeExtension.getEnvironment().put("DOCKER_IMAGE", dockerBuildImage.getImageId().get());
                });
            });
            project.getTasks().named("composeUp", composeUp -> {
                composeUp.dependsOn(buildDockerImageProvider);
            });
        });
    }

    private TaskProvider<DockerBuildImage> createDockerBuildImageTask(Project project,
            Action<? super DockerBuildImage> configure) {
        return project.getTasks().register("buildDockerImage", DockerBuildImage.class, task -> {
            task.setGroup("Docker");
            task.getPull().set(dockerBuildExtension.getPull());
            task.getNoCache().set(dockerBuildExtension.getNoCache());
            task.getRemove().set(dockerBuildExtension.getRemove());
            configure.execute(task);
        });
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

    private Provider<List<String>> getTags() {
        String jenkinsBranch = cleanForDockerTag(JenkinsUtil.getBranch());
        return dockerBuildExtension.getAutomaticTags()
                .flatMap(automaticTags -> {
                    if (!automaticTags) {
                        return dockerBuildExtension.getTags();
                    } else {
                        return dockerBuildExtension.getTags()
                                .map(tags -> {
                                    List<String> newTags = tags.stream().map(tag -> {
                                        if (isMaster()) {
                                            return tag;
                                        } else {
                                            return jenkinsBranch + "-" + tag;
                                        }
                                    }).collect(Collectors.toList());

                                    if (JenkinsUtil.getBuildId() != null) {
                                        if (isMaster()) {
                                            newTags.add("build-" + JenkinsUtil.getBuildId());
                                        } else {
                                            newTags.add(jenkinsBranch + "-build-" + JenkinsUtil.getBuildId());
                                        }
                                    }

                                    if (isMaster()) {
                                        newTags.add("latest");
                                    } else {
                                        newTags.add(jenkinsBranch);
                                    }
                                    newTags.forEach(tag -> {
                                        if(tag.length() > DOCKER_TAG_LENGTH_CONSTRAINT) {
                                            throw new GradleException(DOCKER_TAG_LENGTH_CONSTRAINT_ERRORMSG);
                                        }
                                    });
                                    return newTags;
                                });
                    }
                });
    }

    private boolean isMaster() {
        return "master".equals(JenkinsUtil.getBranch());
    }

    /* Remove illegal characters from tags through encoding*/
    private String cleanForDockerTag(String tag) {
        Pattern illegalCharacters = Pattern.compile("[/\\\\:<>\"?*|]");
        Matcher matcher = illegalCharacters.matcher(tag);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, "_");
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

}
