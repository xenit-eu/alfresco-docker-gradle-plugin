package eu.xenit.gradle.docker.label;

import static eu.xenit.gradle.docker.label.internal.git.JGitInfoProvider.createProviderForProject;

import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage;
import eu.xenit.gradle.docker.label.internal.git.CannotConvertToUrlException;
import eu.xenit.gradle.docker.label.internal.git.JGitInfoProvider;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskCollection;

public class DockerLabelExtension {

    private final static Logger LOGGER = Logging.getLogger(DockerLabelExtension.class);

    private final ProviderFactory providerFactory;
    private final ProjectLayout projectLayout;
    private final TaskCollection<DockerBuildImage> dockerBuildImages;

    private final MapProperty<String, String> gitLabels;

    @Inject
    public DockerLabelExtension(ProviderFactory providerFactory,
            ObjectFactory objectFactory,
            ProjectLayout projectLayout,
            TaskCollection<DockerBuildImage> dockerBuildImages) {
        this.providerFactory = providerFactory;
        this.projectLayout = projectLayout;
        this.dockerBuildImages = dockerBuildImages;

        gitLabels = objectFactory.mapProperty(String.class, String.class);
        addLabels(gitLabels);
    }

    private void addLabels(Provider<? extends Map<String, String>> labels) {
        dockerBuildImages.configureEach(dockerBuildImage -> {
            dockerBuildImage.getLabels().putAll(labels);
        });
    }

    public void fromGit() {
        fromGit(true);
    }

    public void fromGit(boolean enabled) {
        if (enabled) {
            gitLabels.set(providerFactory.provider(() -> {
                Map<String, String> labels = new HashMap<>();
                JGitInfoProvider gitInfoProvider = createProviderForProject(projectLayout);
                String labelPrefix = "eu.xenit.gradle.docker.git.";
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
                }
                return labels;
            }));
        } else {
            gitLabels.empty();
        }
    }

}
