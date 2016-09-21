package eu.xenit.gradle.tasks;

import groovy.lang.Closure;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.InputFile;

import java.io.File;
import java.util.function.Supplier;

public interface WarInputTask extends Task {
    @InputFile
    File getInputWar();

    default void setInputWar(FileCollection configuration)
    {
        dependsOn(configuration);
        setInputWar(configuration::getSingleFile);
    }

    default void setInputWar(WarOutputTask task)
    {
        dependsOn(task);
        setInputWar(task::getOutputWar);
    }

    void setInputWar(Supplier<File> inputWar);

    default void setInputWar(File inputWar) {
        setInputWar(() -> inputWar);
    }

    default void setInputWar(Closure<File> inputWar) {
        setInputWar(() -> inputWar.call());
    }

}
