package eu.xenit.gradle.docker.tasks.internal;

import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import com.bmuschko.gradle.docker.tasks.image.Dockerfile.CopyFile;
import com.bmuschko.gradle.docker.tasks.image.Dockerfile.CopyFileInstruction;
import com.bmuschko.gradle.docker.tasks.image.Dockerfile.Instruction;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import org.gradle.api.Action;
import org.gradle.api.NonNullApi;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

@NonNullApi
public class ConsolidateFileCopyInstructionsAction implements Action<Task> {

    private static final Logger LOGGER = Logging.getLogger(ConsolidateFileCopyInstructionsAction.class);

    @Override
    public void execute(Task task) {
        if (task instanceof Dockerfile) {
            execute((Dockerfile) task);
        } else {
            throw new IllegalArgumentException("ConsolidateFileCopyAction can only be applied to Dockerfile tasks");
        }
    }

    public void execute(Dockerfile dockerfile) {
        List<Instruction> instructions = dockerfile.getInstructions().get();
        List<Instruction> newInstructions = new ArrayList<>(instructions.size());

        @Nullable
        CopyFileInstruction currentCopyInstruction = null;
        int instructionIndex = 0;
        while (instructionIndex < instructions.size()) {
            Instruction instruction = instructions.get(instructionIndex);

            if (instruction instanceof CopyFileInstruction) {
                LOGGER.debug("Processing copy instruction: '{}'", instruction.getText());
                CopyFileInstruction copyFileInstruction = (CopyFileInstruction) instruction;
                if (currentCopyInstruction == null) {
                    LOGGER.debug("Set as current target copy instruction");
                    currentCopyInstruction = copyFileInstruction;
                    instructionIndex++;
                    continue;
                } else if (isSameTarget(copyFileInstruction, currentCopyInstruction)) {
                    LOGGER.debug("Consolidating copy instructions: '{}' + '{}'", currentCopyInstruction.getText(),
                            copyFileInstruction.getText());
                    currentCopyInstruction = consolidateCopy(currentCopyInstruction, copyFileInstruction);
                    LOGGER.debug("Consolidated to '{}'", currentCopyInstruction.getText());
                    instructionIndex++;
                    continue;
                }
            }
            if (currentCopyInstruction != null) {
                LOGGER.debug("Adding consolidated instruction to final instruction stream: '{}'",
                        currentCopyInstruction.getText());
                newInstructions.add(currentCopyInstruction);
                currentCopyInstruction = null;
                // Not bumping instructionIndex, so the new instruction is reprocessed
                continue;
            }
            LOGGER.debug("Adding instruction to instruction stream: '{}'", instruction.getText());
            newInstructions.add(instruction);
            instructionIndex++;
        }

        if (currentCopyInstruction != null) {
            LOGGER.debug("Adding consolidated instruction to final instruction stream: '{}'",
                    currentCopyInstruction.getText());
            newInstructions.add(currentCopyInstruction);
        }

        dockerfile.getInstructions().set(newInstructions);
    }

    private static boolean isSameTarget(CopyFileInstruction a, CopyFileInstruction b) {
        CopyFile copyFileA = a.getFile();
        CopyFile copyFileB = b.getFile();
        // Targets must end with / to be able to copy multiple items to it
        if (!copyFileA.getDest().endsWith("/")) {
            return false;
        }
        if (!Objects.equals(copyFileA.getStage(), copyFileB.getStage())) {
            return false;
        }
        if (!Objects.equals(copyFileA.getChown(), copyFileB.getChown())) {
            return false;
        }
        if (!Objects.equals(copyFileA.getDest(), copyFileB.getDest())) {
            return false;
        }
        return true;
    }

    private static CopyFileInstruction consolidateCopy(CopyFileInstruction a, CopyFileInstruction b) {
        if (!isSameTarget(a, b)) {
            throw new IllegalArgumentException("CopyFileInstructions do not have the same destination");
        }

        CopyFile copyFile = new CopyFile(a.getFile().getSrc() + " " + b.getFile().getSrc(), a.getFile().getDest());
        if (a.getFile().getStage() != null) {
            copyFile.withStage(a.getFile().getStage());
        }
        if (a.getFile().getChown() != null) {
            copyFile.withChown(a.getFile().getChown());
        }

        return new CopyFileInstruction(copyFile);
    }

}
