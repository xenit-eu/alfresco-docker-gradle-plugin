package eu.xenit.gradle.docker.alfresco.tasks;

import eu.xenit.gradle.docker.tasks.DockerfileWithCopyTask;

@Deprecated
public class DockerfileWithWarsTask extends DockerfileWithCopyTask {

    public DockerfileWithWarsTask() {
        super();
        DockerfileWithWarsConventionImpl.applyTo(this);
    }

}
