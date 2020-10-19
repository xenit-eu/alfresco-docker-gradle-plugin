package eu.xenit.gradle.docker;

import eu.xenit.gradle.docker.core.DockerExtension;
import eu.xenit.gradle.docker.internal.Deprecation;
import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;

/**
 * @deprecated Use {@link DockerExtension} instead. Will be removed in 6.0
 */
@Deprecated
public class DockerFileExtension {

    private RegularFileProperty dockerFile;
    private DockerBuildExtension dockerBuild;

    @Inject
    public DockerFileExtension(ObjectFactory objectFactory, DockerExtension dockerExtension) {
        dockerFile = dockerExtension.getDockerFile();
        dockerBuild = objectFactory.newInstance(DockerBuildExtension.class, dockerExtension, "dockerFile");
    }

    public RegularFileProperty getDockerFile() {
        Deprecation.warnDeprecatedExtensionPropertyReplaced("dockerFile.dockerFile", "dockerBuild.dockerFile");
        return dockerFile;
    }

    public DockerBuildExtension getDockerBuild() {
        Deprecation.warnDeprecation("The dockerFile.dockerBuild block is deprecated. Use the dockerBuild block instead.");
        return dockerBuild;
    }

    public void dockerBuild(Action<? super DockerBuildExtension> closure) {
        closure.execute(getDockerBuild());
    }
}
