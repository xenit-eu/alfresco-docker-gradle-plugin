package eu.xenit.gradle.docker;

import groovy.lang.Closure;

import java.io.File;
import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;

/**
 * Created by thijs on 10/25/16.
 */
public class DockerFileExtension {
    private RegularFileProperty dockerFile;
    private DockerBuildExtension dockerBuild;

    @Inject
    public DockerFileExtension(ObjectFactory objectFactory, Project project) {
        dockerFile = objectFactory.fileProperty().convention(project.getLayout().getProjectDirectory().file("Dockerfile"));
        dockerBuild = objectFactory.newInstance(DockerBuildExtension.class, project);
    }

    public RegularFileProperty getDockerFile() {
        return dockerFile;
    }

    public DockerBuildExtension getDockerBuild() {
        return dockerBuild;
    }

    public void dockerBuild(Action<? super DockerBuildExtension> closure) {
        closure.execute(dockerBuild);
    }
}
