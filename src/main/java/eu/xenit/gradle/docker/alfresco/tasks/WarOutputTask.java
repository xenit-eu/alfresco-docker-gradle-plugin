package eu.xenit.gradle.docker.alfresco.tasks;

import org.gradle.api.Task;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;

public interface WarOutputTask extends Task {

    @OutputFile
    RegularFileProperty getOutputWar();
}
