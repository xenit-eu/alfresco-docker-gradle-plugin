package eu.xenit.gradle.docker.alfresco.internal.amp;

import java.io.File;
import java.io.Serializable;
import java.util.Set;

public interface ModuleInformation extends Serializable {

    File getFile();

    String getId();

    String getVersion();

    Set<String> getIds();

    Set<String> getDependencyModuleIds();
}
