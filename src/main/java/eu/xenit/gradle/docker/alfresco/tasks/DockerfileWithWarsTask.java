package eu.xenit.gradle.docker.alfresco.tasks;

import eu.xenit.gradle.docker.alfresco.DockerAlfrescoPlugin;
import eu.xenit.gradle.docker.alfresco.tasks.extension.internal.DockerfileWithLabelExtensionImpl;
import eu.xenit.gradle.docker.alfresco.tasks.extension.internal.DockerfileWithWarsExtensionImpl;
import eu.xenit.gradle.docker.internal.Deprecation;
import eu.xenit.gradle.docker.tasks.DockerfileWithCopyTask;

@Deprecated
public class DockerfileWithWarsTask extends DockerfileWithCopyTask {

    public DockerfileWithWarsTask() {
        super();
        Deprecation.warnDeprecation(
                getClass().getCanonicalName()+" is deprecated. Create a task of type "+DockerfileWithCopyTask.class.getCanonicalName()+" instead and apply the "+ DockerAlfrescoPlugin.PLUGIN_ID+
                " plugin instead.");
        DockerfileWithLabelExtensionImpl.applyTo(this);
        DockerfileWithWarsExtensionImpl.applyTo(this);
    }

}
