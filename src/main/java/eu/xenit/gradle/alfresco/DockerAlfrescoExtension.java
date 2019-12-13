package eu.xenit.gradle.alfresco;

import eu.xenit.gradle.docker.DockerBuildExtension;
import eu.xenit.gradle.docker.internal.Deprecation;
import groovy.lang.Closure;
import java.util.function.Supplier;
import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

/**
 * Created by thijs on 9/21/16.
 */
public class DockerAlfrescoExtension {

    private final Property<String> baseImage;

    /**
     * Don't add the base war, but only the amps, DE, and SM. Use this on images with the correct war already
     * installed. This will then create an image where the last layer only contains the customizations.
     */
    private Property<Boolean> leanImage;

    private DockerBuildExtension dockerBuild;

    @Inject
    public DockerAlfrescoExtension(ObjectFactory objectFactory, Project project) {
        baseImage = objectFactory.property(String.class);
        leanImage = objectFactory.property(Boolean.class).convention(false);
        dockerBuild = objectFactory.newInstance(DockerBuildExtension.class, project);
    }

    public Property<String> getBaseImage() {
        return baseImage;
    }

    public DockerBuildExtension getDockerBuild() {
        return dockerBuild;
    }

    public void dockerBuild(Action<? super DockerBuildExtension> closure) {
        closure.execute(dockerBuild);
    }

    public Property<Boolean> getLeanImage() {
        return leanImage;
    }

}
