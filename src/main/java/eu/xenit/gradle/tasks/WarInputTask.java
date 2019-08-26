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

    /**
     * Internal function so the task can be skipped with no-source when no war is given
     */
    @InputFiles
    @SkipWhenEmpty
    FileCollection get_internal_inputFiles();

    /**
     * Internal function so the file collection can be set from default methods
     */
    void set_internal_inputFiles(FileCollection fileCollection);

    @Internal
    default File getInputWar() {
        return get_internal_inputFiles().getSingleFile();
    }

    default void setInputWar(FileCollection configuration) {
        set_internal_inputFiles(configuration);
    }

    default void setInputWar(WarOutputTask task) {
        set_internal_inputFiles(getProject().files(task));
    }

    default void setInputWar(Supplier<File> inputWar) {
        set_internal_inputFiles(getProject().files(inputWar));
    }

    default void setInputWar(File inputWar) {
        setInputWar(() -> inputWar);
    }

    default void setInputWar(Closure<File> inputWar) {
        setInputWar(() -> inputWar.call());
    }

}
