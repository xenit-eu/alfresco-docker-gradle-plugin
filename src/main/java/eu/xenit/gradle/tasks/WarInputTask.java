package eu.xenit.gradle.tasks;

import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.SkipWhenEmpty;

public interface WarInputTask extends Task {

    @InputFile
    @SkipWhenEmpty
    RegularFileProperty getInputWar();

    default void setInputWar(FileCollection configuration) {
        getInputWar().set(configuration::getSingleFile);
    }

    default void setInputWar(WarOutputTask task) {
        getInputWar().set(task.getOutputWar());
    }

}
