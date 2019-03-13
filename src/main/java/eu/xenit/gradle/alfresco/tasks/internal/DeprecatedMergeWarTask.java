package eu.xenit.gradle.alfresco.tasks.internal;

import eu.xenit.gradle.tasks.LabelConsumerTask;
import eu.xenit.gradle.tasks.MergeWarsTask;
import eu.xenit.gradle.tasks.WarLabelOutputTask;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public class DeprecatedMergeWarTask extends DefaultTask implements LabelConsumerTask, WarLabelOutputTask {

    private MergeWarsTask replacementTask;

    public void setReplacementTask(MergeWarsTask replacementTask) {
        this.replacementTask = replacementTask;
        dependsOn(replacementTask);
    }

    @TaskAction
    public void runTask() {
        getLogger()
                .warn("[eu.xenit.docker-alfresco] The task " + getName()
                        + " is deprecated and will be removed in alfresco-docker-gradle-plugin 5.0.0. Use "
                        + replacementTask.getName() + " instead.");
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
