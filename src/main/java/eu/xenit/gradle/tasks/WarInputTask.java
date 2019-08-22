package eu.xenit.gradle.tasks;

import groovy.lang.Closure;
import java.io.File;
import java.util.function.Supplier;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.SkipWhenEmpty;

public interface WarInputTask extends Task {

    @InputFiles
    @SkipWhenEmpty
    FileCollection getInputFiles_();

    void setInputFiles_(FileCollection fileCollection);

    @Internal
    default File getInputWar() {
        return getInputFiles_().getSingleFile();
    }

    default void setInputWar(FileCollection configuration) {
        setInputFiles_(configuration);
    }

    default void setInputWar(WarOutputTask task) {
        setInputFiles_(getProject().files(task));
    }

    default void setInputWar(Supplier<File> inputWar) {
        setInputFiles_(getProject().files(inputWar));
    }

    default void setInputWar(File inputWar) {
        setInputWar(() -> inputWar);
    }

    default void setInputWar(Closure<File> inputWar) {
        setInputWar(() -> inputWar.call());
    }

}
