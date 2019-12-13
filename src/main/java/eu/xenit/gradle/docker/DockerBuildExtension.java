package eu.xenit.gradle.docker;

import javax.inject.Inject;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

/**
 * Created by thijs on 10/25/16.
 */
public class DockerBuildExtension {

    private final Property<String> repository;
    private final ListProperty<String> tags;

    private final Property<Boolean> pull;

    /**
     * Do not use cache when building the image (default false)
     */
    private final Property<Boolean> noCache;

    private final Property<Boolean> automaticTags;

    private final Property<Boolean> remove;

    @Inject
    public DockerBuildExtension(ObjectFactory objectFactory, Project project) {
        repository = objectFactory.property(String.class);
        tags = objectFactory.listProperty(String.class);
        pull = objectFactory.property(Boolean.class).convention(true);
        noCache = objectFactory.property(Boolean.class).convention(false);
        automaticTags = objectFactory.property(Boolean.class).convention(false);
        remove = objectFactory.property(Boolean.class).convention(true);
    }

    public Property<String> getRepository() {
        return repository;
    }

    public Property<Boolean> getAutomaticTags() {
        return automaticTags;
    }

    public Property<Boolean> getPull() {
        return pull;
    }

    public Property<Boolean> getNoCache() {
        return noCache;
    }

    public Property<Boolean> getRemove() {
        return remove;
    }

    public ListProperty<String> getTags() {
        return tags;
    }
}
