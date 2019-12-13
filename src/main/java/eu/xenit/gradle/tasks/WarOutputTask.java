package eu.xenit.gradle.tasks;

import org.gradle.api.Task;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;

import java.io.File;

public interface WarOutputTask extends Task {
    @OutputFile
    RegularFileProperty getOutputWar();
}
