package eu.xenit.gradle.alfresco;

import eu.xenit.gradle.docker.DockerBuildExtension;
import groovy.lang.Closure;
import java.util.function.Supplier;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

/**
 * Created by thijs on 9/21/16.
 */
public class DockerAlfrescoExtension {

    public static final String MESSAGE_BASE_IMAGE_NOT_SET = "Base image not set. You need to configure your base image in the 'dockerAlfresco' extension block";

    public DockerAlfrescoExtension(Project project) {
        dockerBuild = new DockerBuildExtension(project);
    }
    public Supplier<String> getBaseImageSupplier() {
        return baseImageSupplier;
    }

    public String getBaseImage() {
        return baseImageSupplier.get();
    }

    public void setBaseImage(String baseImage) {
        setBaseImage(() -> baseImage);
    }

    public void setBaseImage(Supplier<String> baseImage){
        this.baseImageSupplierInternal = baseImage;
    }

    public void setBaseImage(Closure<String> baseImage){
        setBaseImage(() -> baseImage.call());
    }

    //Needed to support lazy evaluation
    private Supplier<String> baseImageSupplierInternal;

    private final Supplier<String> baseImageSupplier = () -> {
        if (baseImageSupplierInternal == null) {
            throw new GradleException(MESSAGE_BASE_IMAGE_NOT_SET);
        }
        return baseImageSupplierInternal.get();
    };

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
