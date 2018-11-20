package eu.xenit.gradle.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.bundling.Zip;

public class MergeWarsTask extends Zip implements LabelConsumerTask, WarLabelOutputTask {

    /**
     * WAR file used as output (is created from inputWar)
     */
    private Supplier<File> outputWar = () -> { return getProject().getBuildDir().toPath().resolve("xenit-gradle-plugins").resolve(getName()).resolve(getName()+".war").toFile(); };

    private List<Supplier<Map<String, String>>> labels = new ArrayList<>();

    private final CopySpec childWars;

    public MergeWarsTask() {
        super();
        setExtension("war");
        setDestinationDir(
                getProject().getBuildDir().toPath().resolve("xenit-gradle-plugins").resolve(getName()).toFile());
        setBaseName(getName());
        childWars = getRootSpec().addChildBeforeSpec(getMainSpec());
    }

    @Override
    public void withLabels(Supplier<Map<String, String>> labels) {
        this.labels.add(labels);
    }

    @Override
    @Internal
    public Map<String, String> getLabels() {
        Map<String, String> accumulator = new HashMap<>();
        for (Supplier<Map<String, String>> supplier : labels) {
            accumulator.putAll(supplier.get());
        }
        return accumulator;
    }

    /**
     * WAR files used as input (are not modified)
     * <p>
     * Later files overwrite earlier files
     */
    public void setInputWars(Supplier<List<File>> inputWars) {
        childWars.from((Callable<List<FileTree>>) () -> inputWars.get()
                .stream()
                .map(war -> getProject().zipTree(war))
                .collect(Collectors.toList()));
    }

    @Override
    @OutputFile
    public File getOutputWar() {
        return getDestinationDir().toPath().resolve(getName()).toFile();
    }

}
