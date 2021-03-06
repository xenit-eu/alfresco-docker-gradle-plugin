package eu.xenit.gradle.docker.alfresco.tasks.extension.internal;

import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import com.bmuschko.gradle.docker.tasks.image.Dockerfile.From;
import com.bmuschko.gradle.docker.tasks.image.Dockerfile.Instruction;
import com.bmuschko.gradle.docker.tasks.image.Dockerfile.RunCommandInstruction;
import eu.xenit.gradle.docker.alfresco.internal.version.AlfrescoVersion;
import eu.xenit.gradle.docker.alfresco.tasks.WarLabelOutputTask;
import eu.xenit.gradle.docker.alfresco.tasks.extension.DockerfileWithWarsExtension;
import eu.xenit.gradle.docker.alfresco.tasks.extension.LabelConsumerExtension;
import eu.xenit.gradle.docker.internal.Deprecation;
import eu.xenit.gradle.docker.tasks.extension.internal.DockerfileWithSmartCopyExtensionInternal;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.RegularFile;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.Input;

public class DockerfileWithWarsExtensionImpl implements DockerfileWithWarsExtension, HasPublicType {

    public static final String MESSAGE_BASE_IMAGE_NOT_SET = "Base image not set. You need to configure your base image to build docker images.";

    static final String COMMAND_NO_OP =
            "true # NO-OP from " + DockerfileWithWarsExtensionImpl.class.getCanonicalName();
    static final String COMMAND_ELIDABLE =
            " # Elidable command from " + DockerfileWithWarsExtensionImpl.class.getCanonicalName();

    public static void applyTo(Dockerfile task) {
        DockerfileWithWarsExtension impl = task.getProject().getObjects()
                .newInstance(DockerfileWithWarsExtensionImpl.class, task);
        task.getConvention().getPlugins().put("wars", impl);
        task.doFirst("Remove no-op instructions", new RemoveNoOpInstructionsAction());
        task.doFirst("Elide duplicate version check instructions", new ElideDuplicateVersionChecksAction());
    }

    private final Dockerfile dockerfile;
    private final DockerfileWithSmartCopyExtensionInternal smartCopyExtension;
    private final LabelConsumerExtension labelConsumerExtension;
    private final Project project;
    private final ObjectFactory objectFactory;

    /**
     * Base image used to build the dockerfile
     */
    private final Property<String> baseImage;

    /**
     * Names of WAR files that have already been added
     * <p>
     * This is tracked so multiple WARs with the same name can be overlayed on top of each other,
     * without removing the previously added contents.
     */
    private final Set<String> addedWarNames = new HashSet<>();

    /**
     * Target directory inside the docker container where war files will be placed
     */
    private final Property<String> targetDirectory;

    /**
     * Before adding the new war to the image, remove the expanded folder in the webapps.
     */
    private final Property<Boolean> removeExistingWar;

    /**
     * Check if Alfresco version that is already present in the container matches the Alfresco version of the war that will be added.
     */
    private final Property<Boolean> checkAlfrescoVersion;

    @Override
    @Input
    public Property<String> getTargetDirectory() {
        return targetDirectory;
    }

    @Override
    @Input
    public Property<Boolean> getRemoveExistingWar() {
        return removeExistingWar;
    }

    @Override
    @Input
    public Property<Boolean> getCheckAlfrescoVersion() {
        return checkAlfrescoVersion;
    }

    @Override
    @Input
    public Property<String> getBaseImage() {
        return baseImage;
    }

    @Inject
    public DockerfileWithWarsExtensionImpl(Dockerfile dockerfile) {
        this.dockerfile = dockerfile;
        this.smartCopyExtension = DockerfileWithSmartCopyExtensionInternal.get(dockerfile);
        this.labelConsumerExtension = LabelConsumerExtension.get(dockerfile);
        this.project = dockerfile.getProject();
        this.objectFactory = project.getObjects();

        this.baseImage = objectFactory.property(String.class).convention((String) null);
        this.targetDirectory = objectFactory.property(String.class).convention("/usr/local/tomcat/webapps/");
        this.removeExistingWar = objectFactory.property(Boolean.class).convention(true);
        this.checkAlfrescoVersion = objectFactory.property(Boolean.class)
                .convention(this.removeExistingWar.map(remove -> !remove));
        dockerfile.from(baseImage.map(From::new));
        baseImage.set((String) null);
        dockerfile.doFirst(new ValidateBaseImageSet());
    }


    @Override
    public void addWar(String name, WarLabelOutputTask task) {
        dockerfile.dependsOn(task);
        addWar(name, task.getOutputWar());
        labelConsumerExtension.withLabels(task);
    }

    @Override
    public void addWar(String name, Provider<RegularFile> regularFileProvider) {
        addWar0(name, regularFileProvider.map(RegularFile::getAsFile));
    }

    @Override
    public void addWar(String name, java.io.File file) {
        addWar0(name, project.provider(() -> file));
    }

    private void addWar0(String name, Provider<java.io.File> file) {
        if (!addedWarNames.contains(name)) {
            dockerfile.runCommand(getRemoveExistingWar()
                    .map(removeWar -> {
                        if (!Boolean.TRUE.equals(removeWar)) {
                            return COMMAND_NO_OP;
                        }
                        return "rm -rf " + getTargetDirectory().get() + name;
                    }));
        }
        dockerfile.runCommand(getCheckAlfrescoVersion().flatMap(checkVersion -> {
            if (!Boolean.TRUE.equals(checkVersion)) {
                return project.provider(() -> COMMAND_NO_OP);
            }
            return file.map(f -> {
                try {
                    AlfrescoVersion alfrescoVersion = AlfrescoVersion.fromAlfrescoWar(f.toPath());
                    if (alfrescoVersion != null) {
                        return alfrescoVersion.getCheckCommand(getTargetDirectory().get() + name)
                                + COMMAND_ELIDABLE;
                    }
                    return COMMAND_NO_OP;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });

        }));
        smartCopyExtension.smartCopy(file, getTargetDirectory().map(target -> target + name), project::zipTree);
        addedWarNames.add(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public void addWar(String name, Supplier<File> file) {
        Deprecation.warnDeprecatedReplaced("addWar(String name, Supplier<File> file)",
                "addWar(String name, Provider<RegularFile> fileProvider)");
        addWar0(name, project.provider(file::get));
    }

    @Override
    public void addWar(String name, Configuration configuration) {
        dockerfile.dependsOn(configuration);
        addWar0(name, project.provider(configuration::getSingleFile));
    }

    @Override
    public TypeOf<?> getPublicType() {
        return TypeOf.typeOf(DockerfileWithWarsExtension.class);
    }

    public static class ValidateBaseImageSet implements Action<Task> {

        @Override
        public void execute(Task task) {
            if (task instanceof Dockerfile) {
                execute((Dockerfile) task);
            } else {
                throw new IllegalArgumentException("Task must be a Dockerfile");
            }
        }

        public void execute(Dockerfile dockerfile) {
            DockerfileWithWarsExtension extension = DockerfileWithWarsExtension.get(dockerfile);
            if (extension.getBaseImage().getOrNull() == null) {
                throw new IllegalStateException(MESSAGE_BASE_IMAGE_NOT_SET);
            }
        }
    }

    public static class RemoveNoOpInstructionsAction implements Action<Task> {

        @Override
        public void execute(Task task) {
            if (task instanceof Dockerfile) {
                execute((Dockerfile) task);
            } else {
                throw new IllegalArgumentException("Task must be a Dockerfile");
            }
        }

        public void execute(Dockerfile dockerfile) {
            List<Instruction> instructions = dockerfile.getInstructions().get()
                    .stream()
                    .filter(instruction -> instruction.getText() != null)
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
            if (task instanceof Dockerfile) {
                execute((Dockerfile) task);
            } else {
                throw new IllegalArgumentException("Task must be a Dockerfile");
            }
        }

        public void execute(Dockerfile dockerfile) {
            List<Instruction> instructions = dockerfile.getInstructions().get();
            Set<String> elidableCommands = new HashSet<>();
            List<Instruction> newInstructions = new ArrayList<>(instructions.size());

            for (Instruction instruction : instructions) {
                if (instruction instanceof RunCommandInstruction && instruction.getText() != null && instruction
                        .getText().endsWith(COMMAND_ELIDABLE)) {
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
