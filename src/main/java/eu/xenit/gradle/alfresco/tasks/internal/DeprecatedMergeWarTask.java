package eu.xenit.gradle.alfresco.tasks.internal;

import eu.xenit.gradle.docker.internal.Deprecation;
import eu.xenit.gradle.tasks.LabelConsumerTask;
import eu.xenit.gradle.tasks.MergeWarsTask;
import eu.xenit.gradle.tasks.WarLabelOutputTask;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

/**
 * @deprecated since 4.1.0, will be removed in 5.0.0
 */
@Deprecated
public class DeprecatedMergeWarTask extends DefaultTask implements LabelConsumerTask, WarLabelOutputTask {

    private MergeWarsTask replacementTask;

    public void setReplacementTask(MergeWarsTask replacementTask) {
        this.replacementTask = replacementTask;
        dependsOn(replacementTask);
    }

    @TaskAction
    public void runTask() {
        Deprecation.warnDeprecation("The task "+getName()+" is deprecated and will be removed in 5.0.0. Use "+replacementTask.getName()+" instead.");
    }

    @Override
    public void withLabels(Supplier<Map<String, String>> labels) {
        replacementTask.withLabels(labels);

    }

    @Override
    @Input
    public Map<String, String> getLabels() {
        return replacementTask.getLabels();
    }

    public void setInputWars(Supplier<List<File>> inputWars) {
        replacementTask.setInputWars(inputWars);
    }

    @Override
    @OutputFile
    public File getOutputWar() {
        return replacementTask.getOutputWar();
    }
}
