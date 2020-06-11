package eu.xenit.gradle.docker.alfresco.tasks;


import eu.xenit.gradle.docker.alfresco.internal.version.AlfrescoVersion;
import eu.xenit.gradle.docker.internal.Deprecation;
import eu.xenit.gradle.docker.tasks.DockerfileWithCopyTask;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFile;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.GradleVersion;

public class DockerfileWithWarsTask extends DockerfileWithCopyTask implements LabelConsumerTask {

    public static final String MESSAGE_BASE_IMAGE_NOT_SET = "Base image not set. You need to configure your base image to build docker images.";
    private static final String COMMAND_NO_OP = "true # NO-OP from " + DockerfileWithWarsTask.class.getCanonicalName();
    private static final String COMMAND_ELIDABLE =
            " # Elidable command from " + DockerfileWithWarsTask.class.getCanonicalName();
    /**
     * Base image used to build the dockerfile
     */
    private Property<String> baseImage = getProject().getObjects().property(String.class);

    /**
     * Map of labels to add to the dockerfile
     */
    private final MapProperty<String, String> labels = getProject().getObjects()
            .mapProperty(String.class, String.class);

    /**
     * Names of WAR files that have already been added
     *
     * This is tracked so multiple WARs with the same name can be overlayed on top of each other,
     * without removing the previously added contents.
     */
    private final Set<String> addedWarNames = new HashSet<>();

    /**
     * Target directory inside the docker container where war files will be placed
     */
    private Property<String> targetDirectory = getProject().getObjects().property(String.class)
            .convention("/usr/local/tomcat/webapps/");

    public DockerfileWithWarsTask() {
        if (GradleVersion.current().compareTo(GradleVersion.version("5.6")) >= 0) {
            from(baseImage.orElse(getProject().provider(() -> {
                throw new IllegalStateException(MESSAGE_BASE_IMAGE_NOT_SET);
            })).map(From::new));
        } else {
            from(baseImage.map(From::new));
        }
        baseImage.set((String) null);

        // This runs in afterEvaluate, because we want this doFirst action to really run *before*
        // any other doFirst actions, as we need to clean up our own mess with no-op instructions
        getProject().afterEvaluate(p -> {
            doFirst("Remove no-op instructions", new RemoveNoOpInstructionsAction());
            doFirst("Elide duplicate version check instructions", new ElideDuplicateVersionChecksAction());
        });
    }

    @Input
    public Property<String> getTargetDirectory() {
        return targetDirectory;
    }


    /**
     * Before adding the new war to the image, remove the expanded folder in the webapps.
     */
    private Property<Boolean> removeExistingWar = getProject().getObjects().property(Boolean.class).convention(true);


    @Input
    public Property<Boolean> getRemoveExistingWar() {
        return removeExistingWar;
    }

    /**
     * Check if Alfresco version that is already present in the container matches the Alfresco version of the war that will be added.
     */
    private Property<Boolean> checkAlfrescoVersion = getProject().getObjects().property(Boolean.class)
            .convention(getRemoveExistingWar().map(b -> !b));

    @Input
    public Property<Boolean> getCheckAlfrescoVersion() {
        return checkAlfrescoVersion;
    }

    @Input
    public Property<String> getBaseImage() {
        return baseImage;
    }

    public void addWar(String name, WarLabelOutputTask task) {
        dependsOn(task);
        addWar(name, task.getOutputWar());
        withLabels(task);
    }

    public void addWar(String name, Provider<RegularFile> regularFileProvider) {
        _addWar(name, regularFileProvider.map(RegularFile::getAsFile));
    }

    public void addWar(String name, java.io.File file) {
        _addWar(name, getProject().provider(() -> file));
    }

    private void _addWar(String name, Provider<java.io.File> file) {
        if (!addedWarNames.contains(name)) {
            runCommand(getRemoveExistingWar()
                    .map(removeWar -> {
                        if (!Boolean.TRUE.equals(removeWar)) {
                            return COMMAND_NO_OP;
                        }
                        return "rm -rf " + getTargetDirectory().get() + name;
                    }));
        }
        runCommand(getCheckAlfrescoVersion().flatMap(checkVersion -> {
            if (!Boolean.TRUE.equals(checkVersion)) {
                return getProject().provider(() -> COMMAND_NO_OP);
            }
            return file.map(f -> {
                try {
                    AlfrescoVersion alfrescoVersion = AlfrescoVersion.fromAlfrescoWar(f.toPath());
                    if (alfrescoVersion != null) {
                        return alfrescoVersion.getCheckCommand(getTargetDirectory().get() + name) + COMMAND_ELIDABLE;
                    }
                    return COMMAND_NO_OP;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });

        }));
        ConfigurableFileCollection fc = getProject().files();
        fc.from(getProject().provider(() -> {
            java.io.File resolvedFile = file.getOrNull();
            if (resolvedFile == null) {
                // Return empty collection when we resolved to null, else the FileCollection will throw an exception,
                // because it tries to unpack the provider with Provider#get()
                return getProject().files();
            }
            return getProject().zipTree(resolvedFile);
        }));
        smartCopy(fc, getTargetDirectory().map(target -> target + name));
        addedWarNames.add(name);
    }

    @Deprecated
    public void addWar(String name, Supplier<java.io.File> file) {
        Deprecation.warnDeprecatedReplaced("addWar(String name, Supplier<File> file)",
                "addWar(String name, Provider<RegularFile> fileProvider)");
        _addWar(name, getProject().provider(file::get));
    }

    public void addWar(String name, Configuration configuration) {
        dependsOn(configuration);
        _addWar(name, getProject().provider(configuration::getSingleFile));
    }

    @TaskAction
    @Override
    public void create() {
        // LABEL
        if (!getLabels().get().isEmpty()) {
            label(getLabels());
        }

        super.create();
    }

    @Override
    public void withLabels(Provider<Map<String, String>> labels) {
        this.labels.putAll(labels);
    }

    @Input
    public MapProperty<String, String> getLabels() {
        return labels;
    }

    public static class RemoveNoOpInstructionsAction implements Action<Task> {

        @Override
        public void execute(Task task) {
            if (task instanceof DockerfileWithWarsTask) {
                execute((DockerfileWithWarsTask) task);
            } else {
                throw new IllegalArgumentException("Task must be a DockerfileWithWarsTask");
            }
        }

        public void execute(DockerfileWithWarsTask dockerfile) {
            List<Instruction> instructions = dockerfile.getInstructions().get()
                    .stream()
                    .filter(instruction -> !(instruction instanceof RunCommandInstruction && Objects
                            .equals(instruction.getText(), instruction.getKeyword() + " " + COMMAND_NO_OP)))
                    .collect(Collectors.toList());
            dockerfile.getInstructions().set(instructions);
        }
    }

    public static class ElideDuplicateVersionChecksAction implements Action<Task> {

        private static final Logger LOGGER = Logging.getLogger(ElideDuplicateVersionChecksAction.class);

        @Override
        public void execute(Task task) {
            if (task instanceof DockerfileWithWarsTask) {
                execute((DockerfileWithWarsTask) task);
            } else {
                throw new IllegalArgumentException("Task must be a DockerfileWithWarsTask");
            }
        }

        public void execute(DockerfileWithWarsTask dockerfile) {
            List<Instruction> instructions = dockerfile.getInstructions().get();
            Set<String> elidableCommands = new HashSet<>();
            List<Instruction> newInstructions = new ArrayList<>(instructions.size());

            for (Instruction instruction : instructions) {
                if (instruction instanceof RunCommandInstruction && instruction.getText() != null && instruction.getText().endsWith(COMMAND_ELIDABLE)) {
                    if (!elidableCommands.add(instruction.getText())) {
                        LOGGER.debug("Eliding command '{}' because we have already seen it.",
                                instruction.getText());
                        continue;
                    } else {
                        String command = instruction.getText();
                        String newCommand = command.substring(instruction.getKeyword().length() + 1,
                                command.length() - COMMAND_ELIDABLE.length());
                        LOGGER.debug("Stripped elidable suffix from command: '{}'", newCommand);
                        instruction = new RunCommandInstruction(newCommand);
                    }
                }
                newInstructions.add(instruction);
            }
            dockerfile.getInstructions().set(newInstructions);
        }

    }
}
