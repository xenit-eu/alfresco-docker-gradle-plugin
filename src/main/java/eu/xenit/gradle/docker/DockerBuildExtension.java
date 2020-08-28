package eu.xenit.gradle.docker;

import eu.xenit.gradle.docker.autotag.DockerAutotagExtension;
import eu.xenit.gradle.docker.core.DockerExtension;
import eu.xenit.gradle.docker.internal.Deprecation;
import java.util.HashSet;
import javax.inject.Inject;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

public class DockerBuildExtension {

    private final Property<String> repository;
    private final String deprecationPrefix;
    private final ListProperty<String> tags;

    private final Property<Boolean> pull;

    /**
     * Do not use cache when building the image (default false)
     */
    private final Property<Boolean> noCache;

    private final Property<Boolean> automaticTags;

    private final Property<Boolean> remove;

    @Inject
    public DockerBuildExtension(ObjectFactory objectFactory, DockerExtension dockerExtension,
            String deprecationPrefix) {
        repository = objectFactory.property(String.class);
        this.deprecationPrefix = deprecationPrefix;
        dockerExtension.getRepositories().add(repository);
        tags = objectFactory.listProperty(String.class);
        dockerExtension.getTags().addAll(tags.flatMap(
                t -> getAutomaticTags().map(automaticTags ->
                        automaticTags?dockerExtension.getExtensions().getByType(DockerAutotagExtension.class)
                        .legacyTags(new HashSet<>(t)):new HashSet<>(t)
                )
        ));
        pull = objectFactory.property(Boolean.class).convention(true);
        noCache = objectFactory.property(Boolean.class).convention(false);
        automaticTags = objectFactory.property(Boolean.class).convention(false);
        remove = objectFactory.property(Boolean.class).convention(true);
    }

    public Property<String> getRepository() {
        Deprecation.warnDeprecatedExtensionPropertyReplaced(deprecationPrefix+"dockerBuild.repository", "dockerBuild.repositories");
        return repository;
    }

    public Property<Boolean> getAutomaticTags() {
        return automaticTags;
    }

    public Property<Boolean> getPull() {
        Deprecation.warnDeprecatedExtensionProperty(deprecationPrefix+"dockerBuild.pull", "Set the pull property directly on the buildDockerImage task instead.");
        return pull;
    }

    public Property<Boolean> getNoCache() {
        Deprecation.warnDeprecatedExtensionProperty(deprecationPrefix+"dockerBuild.noCache", "Set the noCache property directly on the buildDockerImage task instead.");
        return noCache;
    }

    public Property<Boolean> getRemove() {
        Deprecation.warnDeprecatedExtensionProperty(deprecationPrefix+"dockerBuild.remove", "Set the remove property directly on the buildDockerImage task instead.");
        return remove;
    }

    public ListProperty<String> getTags() {
        Deprecation.warnDeprecatedExtensionPropertyReplaced(deprecationPrefix+"dockerBuild.tags", "dockerBuild.tags");
        return tags;
    }
}
