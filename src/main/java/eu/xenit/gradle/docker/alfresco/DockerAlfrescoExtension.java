package eu.xenit.gradle.docker.alfresco;

import eu.xenit.gradle.docker.DockerBuildExtension;
import eu.xenit.gradle.docker.core.DockerExtension;
import eu.xenit.gradle.docker.internal.Deprecation;
import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

/**
 * @deprecated Use {@link AlfrescoDockerExtension} instead. Will be removed in 6.0
 */
@Deprecated
public class DockerAlfrescoExtension {

    private final Property<String> baseImage;

    /**
     * Don't add the base war, but only the amps, DE, and SM. Use this on images with the correct war already
     * installed. This will then create an image where the last layer only contains the customizations.
     */
    private Property<Boolean> leanImage;

    private DockerBuildExtension dockerBuild;

    @Inject
    public DockerAlfrescoExtension(ObjectFactory objectFactory, DockerExtension dockerExtension) {
        AlfrescoDockerExtension alfrescoDockerExtension = dockerExtension.getExtensions().findByType(AlfrescoDockerExtension.class);
        baseImage = alfrescoDockerExtension.getBaseImage();
        leanImage = alfrescoDockerExtension.getLeanImage();
        dockerBuild = objectFactory.newInstance(DockerBuildExtension.class, dockerExtension, "dockerAlfresco");
    }

    public Property<String> getBaseImage() {
        Deprecation.warnDeprecatedExtensionPropertyReplaced("dockerAlfresco.baseImage", "dockerBuild.alfresco.baseImage");
        return baseImage;
    }

    public DockerBuildExtension getDockerBuild() {
        Deprecation.warnDeprecation("The dockerAlfresco.dockerBuild block is deprecated. Use the dockerBuild block instead.");
        return dockerBuild;
    }

    public void dockerBuild(Action<? super DockerBuildExtension> closure) {
        closure.execute(getDockerBuild());
    }

    public Property<Boolean> getLeanImage() {
        Deprecation.warnDeprecatedExtensionPropertyReplaced("dockerAlfresco.leanImage", "dockerBuild.alfresco.leanImage");
        return leanImage;
    }

}
