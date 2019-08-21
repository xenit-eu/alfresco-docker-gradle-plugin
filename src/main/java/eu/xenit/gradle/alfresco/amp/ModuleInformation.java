package eu.xenit.gradle.alfresco.amp;

import java.io.File;
import java.util.Set;

public interface ModuleInformation {

    File getFile();

    String getId();

    Set<String> getIds();

    Set<String> getDependencyModuleIds();
}
