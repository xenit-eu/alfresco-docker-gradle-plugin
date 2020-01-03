package eu.xenit.gradle.tasks;

import org.gradle.api.Task;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.OutputFile;

public interface WarOutputTask extends Task {

    @OutputFile
    RegularFileProperty getOutputWar();
}
