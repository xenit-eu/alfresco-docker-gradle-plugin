package eu.xenit.gradle.alfresco.amp;

import java.io.File;
import java.io.Serializable;
import java.util.Set;

public interface ModuleInformation extends Serializable {

    File getFile();

    String getId();

    Set<String> getIds();

    Set<String> getDependencyModuleIds();
}
