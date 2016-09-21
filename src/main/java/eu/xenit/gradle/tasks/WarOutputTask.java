package eu.xenit.gradle.tasks;

import groovy.lang.Closure;
import org.gradle.api.Task;
import org.gradle.api.tasks.OutputFile;

import java.io.File;
import java.util.function.Supplier;

public interface WarOutputTask extends Task {
    @OutputFile
    File getOutputWar();

    void setOutputWar(Supplier<File> outputWar);

    default void setOutputWar(Closure<File> outputWar) {
        setOutputWar(() -> outputWar.call());
    }

    default void setOutputWar(File outputWar) {
        setOutputWar(() -> outputWar);
    }

}
