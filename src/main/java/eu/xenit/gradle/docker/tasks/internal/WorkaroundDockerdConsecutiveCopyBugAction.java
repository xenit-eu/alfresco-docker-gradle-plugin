package eu.xenit.gradle.docker.tasks.internal;

import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import com.bmuschko.gradle.docker.tasks.image.Dockerfile.CopyFile;
import com.bmuschko.gradle.docker.tasks.image.Dockerfile.CopyFileInstruction;
import com.bmuschko.gradle.docker.tasks.image.Dockerfile.Instruction;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.gradle.api.Action;
import org.gradle.api.NonNullApi;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

@NonNullApi
public class WorkaroundDockerdConsecutiveCopyBugAction implements Action<Task> {

    private static final Logger LOGGER = Logging.getLogger(WorkaroundDockerdConsecutiveCopyBugAction.class);
    public static final String FEATURE_FLAG = "eu.xenit.docker.flags.workaround-dockerd-consecutive-copy-bug";
    private static final String CONSECUTIVE_COPIES_PROP = "eu.xenit.docker.flags.workaround-dockerd-consecutive-copy-bug.maxConsecutiveCopies";
    private static final int DEFAULT_MAX_CONSECUTIVE_COPIES = 6;

    @Override
    public void execute(Task task) {
        if (task instanceof Dockerfile) {
            execute((Dockerfile) task);
        } else {
            throw new IllegalArgumentException(
                    "WorkaroundDockerdConsecutiveCopyBugAction can only be applied to Dockerfile tasks");
        }
    }

    public static int getMaxConsecutiveCopies(Task task) {
        try {
            int consecutiveCopies = Optional.ofNullable(task.getProject().findProperty(CONSECUTIVE_COPIES_PROP))
                    .map(Object::toString)
                    .map(Integer::parseInt)
                    .orElse(DEFAULT_MAX_CONSECUTIVE_COPIES);
            if (consecutiveCopies <= 0) {
                throw new IllegalArgumentException("Invalid value for property " + CONSECUTIVE_COPIES_PROP
                        + ": Must be a strictly positive integer.");
            }
            return consecutiveCopies;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid value for property " + CONSECUTIVE_COPIES_PROP + ": " + e.getMessage(), e);
        }
    }

    public void execute(Dockerfile dockerfile) {
        final int maxConsecutiveCopies = getMaxConsecutiveCopies(dockerfile);
        List<Instruction> instructions = dockerfile.getInstructions().get();
        List<Instruction> intermediateInstructions = new ArrayList<>(instructions.size());

        // First split up COPY commands with too many instructions to many COPY commands
        for (Instruction instruction : instructions) {
            if (instruction instanceof CopyFileInstruction) {
                String source = ((CopyFileInstruction) instruction).getFile().getSrc();
                int numberOfSources = countSpaces(source) + 1;

                if (numberOfSources > maxConsecutiveCopies) {
                    LOGGER.debug("Splitting up COPY instruction with more than {} sources", maxConsecutiveCopies);
                    List<Instruction> additionalInstructions = splitCopyInstruction((CopyFileInstruction) instruction,
                            maxConsecutiveCopies);
                    LOGGER.debug("Replacing instruction '{}' with {}", instruction.getText(),
                            additionalInstructions.stream().map(Instruction::getText).toArray());
                    intermediateInstructions.addAll(additionalInstructions);
                    continue;
                }
            }
            intermediateInstructions.add(instruction);
        }

        // Then insert additional RUN instructions between COPY commands that would be too many in sequence
        List<Instruction> newInstructions = new ArrayList<>(instructions.size());
        int consecutiveCopyInstructions = 0;

        for (Instruction instruction : intermediateInstructions) {
            if (instruction instanceof CopyFileInstruction) {
                String source = ((CopyFileInstruction) instruction).getFile().getSrc();
                int numberOfSources = countSpaces(source) + 1;
                LOGGER.debug("Evaluating COPY instruction: '{}' found {} sources", instruction.getText(),
                        numberOfSources);
                consecutiveCopyInstructions += numberOfSources;
            } else {
                consecutiveCopyInstructions = 0;
            }
            if (consecutiveCopyInstructions > maxConsecutiveCopies) {
                LOGGER.debug("Inserting dummy instruction into instruction stream");
                consecutiveCopyInstructions = 0;
                newInstructions.add(new Dockerfile.RunCommandInstruction("true"));
            }
            LOGGER.debug("Adding instruction to instruction stream: '{}'", instruction.getText());
            newInstructions.add(instruction);
        }

        dockerfile.getInstructions().set(newInstructions);
    }


    private static int countSpaces(String str) {
        return str.length() - str.replace(" ", "").length();
    }

    private static List<Instruction> splitCopyInstruction(CopyFileInstruction instruction, int maxConsecutiveCopies) {
        String sources = instruction.getFile().getSrc();
        List<String> separateSources = splitCopySources(sources);
        List<String> groupedSources = groupCopySources(separateSources, maxConsecutiveCopies);

        List<Instruction> newInstructions = new ArrayList<>();
        for (String source : groupedSources) {
            CopyFile copyFile = new CopyFile(source, instruction.getFile().getDest());
            copyFile.withStage(instruction.getFile().getStage()).withChown(instruction.getFile().getChown());
            newInstructions.add(new CopyFileInstruction(copyFile));
        }
        return newInstructions;
    }

    private static List<String> groupCopySources(List<String> separateSources, int maxConsecutiveCopies) {
        List<String> groupedSources = new ArrayList<>();
        @Nullable
        StringBuilder currentSourcesGroup = null;

        int numberOfSources = 0;
        for (String source : separateSources) {
            if (numberOfSources < maxConsecutiveCopies) {
                if (currentSourcesGroup != null) {
                    currentSourcesGroup.append(" ");
                } else {
                    currentSourcesGroup = new StringBuilder();
                }
                currentSourcesGroup.append(source);
                numberOfSources++;
            } else {
                groupedSources.add(currentSourcesGroup.toString());
                currentSourcesGroup = new StringBuilder(source);
                numberOfSources = 0;
            }
        }
        if (currentSourcesGroup != null) {
            groupedSources.add(currentSourcesGroup.toString());
        }
        return groupedSources;
    }

    private static List<String> splitCopySources(String sources) {
        List<String> splitSources = new ArrayList<>();

        @Nullable
        StringBuilder currentSource = null;
        for (int i = 0; i < sources.length(); i++) {
            char currentChar = sources.charAt(i);
            if (currentChar == ' ') {
                if (currentSource != null) {
                    splitSources.add(currentSource.toString());
                }
                currentSource = null;
            } else {
                if (currentSource == null) {
                    currentSource = new StringBuilder();
                }
                currentSource.append(currentChar);
            }
        }

        if (currentSource != null) {
            splitSources.add(currentSource.toString());
        }

        return splitSources;
    }
}
