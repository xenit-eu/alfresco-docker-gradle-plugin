package eu.xenit.gradle.alfresco;

import eu.xenit.gradle.docker.DockerBuildExtension;
import eu.xenit.gradle.docker.internal.Deprecation;
import groovy.lang.Closure;
import java.util.function.Supplier;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

/**
 * Created by thijs on 9/21/16.
 */
public class DockerAlfrescoExtension {

    private final Project project;

    private final Property<String> baseImage;

    public DockerAlfrescoExtension(Project project) {
        dockerBuild = new DockerBuildExtension(project);
        this.project = project;
        baseImage = project.getObjects().property(String.class);
    }

    Provider<String> getBaseImageProperty() {
        return baseImage;
    }

    public String getBaseImage() {
        return baseImage.getOrNull();
    }

    public void setBaseImage(String baseImage) {
        this.baseImage.set(baseImage);
    }

    public void setBaseImage(Provider<String> baseImage) {
        this.baseImage.set(baseImage);
    }

    /**
     * @deprecated in 4.1.0, removed in 5.0.0. Use {@link #setBaseImage(Provider)} instead.
     */
    @Deprecated
    public void setBaseImage(Supplier<String> baseImage) {
        Deprecation.warnDeprecatedReplacedBy("setBaseImage(Provider<String))");
        setBaseImage(project.provider(baseImage::get));
    }

    /**
     * @deprecated in 4.1.0, removed in 5.0.0. Use {@link #setBaseImage(Provider)} instead.
     */
    @Deprecated
    public void setBaseImage(Closure<String> baseImage) {
        Deprecation.warnDeprecatedReplacedBy("setBaseImage(Provider<String))");
        setBaseImage(baseImage::call);
    }

    public DockerBuildExtension getDockerBuild() {
        return dockerBuild;
    }

    public void setDockerBuild(DockerBuildExtension dockerBuild) {
        this.dockerBuild = dockerBuild;
    }

    private DockerBuildExtension dockerBuild;

    public void dockerBuild(Action<? super DockerBuildExtension> closure) {
        closure.execute(dockerBuild);
    }

    /**
     * Don't add the base war, but only the amps, DE, and SM. Use this on images with the correct war already
     * installed. This will then create an image where the last layer only contains the customizations.
     */
    private boolean leanImage = false;


    public boolean getLeanImage() {
        return leanImage;
    }

    public void setLeanImage(boolean leanImage) {
        this.leanImage = leanImage;
    }
}
