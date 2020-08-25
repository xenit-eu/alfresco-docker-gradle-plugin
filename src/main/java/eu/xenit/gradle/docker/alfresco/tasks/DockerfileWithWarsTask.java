package eu.xenit.gradle.docker.alfresco.tasks;

import eu.xenit.gradle.docker.alfresco.DockerAlfrescoPlugin;
import eu.xenit.gradle.docker.alfresco.tasks.extension.internal.DockerfileWithLabelExtensionImpl;
import eu.xenit.gradle.docker.alfresco.tasks.extension.internal.DockerfileWithWarsExtensionImpl;
import eu.xenit.gradle.docker.internal.Deprecation;
import eu.xenit.gradle.docker.tasks.DockerfileWithCopyTask;

/**
 * @deprecated since 5.1.0, will be removed in 6.0.0. Use {@link eu.xenit.gradle.docker.alfresco.tasks.extension.DockerfileWithWarsExtension} instead.
 */
@Deprecated
public class DockerfileWithWarsTask extends DockerfileWithCopyTask {

    public DockerfileWithWarsTask() {
        super();
        Deprecation.warnDeprecation(
                getClass().getCanonicalName() + " is deprecated. Create a task of type " + DockerfileWithCopyTask.class
                        .getCanonicalName() + " instead and apply the " + DockerAlfrescoPlugin.PLUGIN_ID +
                        " plugin instead.");
        DockerfileWithLabelExtensionImpl.applyTo(this);
        DockerfileWithWarsExtensionImpl.applyTo(this);
    }

}
