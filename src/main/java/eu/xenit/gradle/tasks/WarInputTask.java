package eu.xenit.gradle.tasks;

import groovy.lang.Closure;
import java.io.File;
import java.util.function.Supplier;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
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
