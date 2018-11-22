package eu.xenit.gradle.tasks;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static eu.xenit.gradle.alfresco.DockerAlfrescoPlugin.LABEL_PREFIX;

/**
 * Created by thijs on 31/05/17.
 * This task can get a configuration as input and resolves it to a file as output. It is mainly used to pass the
 * filename in the labels.
 */
@Deprecated
public class ResolveWarTask extends DefaultTask implements WarEnrichmentTask {
    private Supplier<File> war;
    private List<Supplier<Map<String, String>>> labels = new ArrayList<>();

    @Override
    @InputFile
    public File getInputWar() {
        return war.get();
    }

    @Override
    public void setInputWar(Supplier<File> inputWar) {
        war = inputWar;
    }

    private Supplier<File> outputWar = () -> { return getProject().getBuildDir().toPath().resolve("xenit-gradle-plugins").resolve(getName()).resolve(getName()+".war").toFile(); };

    @Override
    @OutputFile
    public File getOutputWar() {
        return outputWar.get();
    }

    @Override
    public void withLabels(Supplier<Map<String, String>> labels) {
        this.labels.add(labels);
    }

    @Override
    @Internal
    public Map<String, String> getLabels() {
        Map<String, String> accumulator = new HashMap<>();
        accumulator.put(LABEL_PREFIX+getName(), getInputWar().getName());
        for (Supplier<Map<String, String>> supplier : labels) {
            accumulator.putAll(supplier.get());
        }
        return accumulator;
    }


    @TaskAction
    public void copyWar() throws IOException {
        getLogger()
                .warn("[eu.xenit.docker] The ResolveWarTask type is deprecated and will be removed in xenit-gradle-plugins 5.0.0. Use StripAlfrescoWarTask instead, or use the Configuration directly.");
        File outputWar = getOutputWar();
        FileUtils.copyFile(getInputWar(), outputWar);
    }

}
