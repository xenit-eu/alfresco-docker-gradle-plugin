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
public class Workaround7ConsecutiveCopyDockerBugAction implements Action<Task> {

    private static final Logger LOGGER = Logging.getLogger(Workaround7ConsecutiveCopyDockerBugAction.class);
    public static final String FEATURE_FLAG = "eu.xenit.docker.flags.workaround-dockerd-consecutive-copy-bug";

    @Override
    public void execute(Task task) {
        if (task instanceof Dockerfile) {
            execute((Dockerfile) task);
        } else {
            throw new IllegalArgumentException(
                    "Workaround2ConsecutiveCopyDockerBugAction can only be applied to Dockerfile tasks");
        }
    }

    public void execute(Dockerfile dockerfile) {
        List<Instruction> instructions = dockerfile.getInstructions().get();
        List<Instruction> newInstructions = new ArrayList<>(instructions.size());

        int consecutiveCopyInstructions = 0;

        for (Instruction instruction : instructions) {
            if (instruction instanceof CopyFileInstruction) {
                String source = ((CopyFileInstruction) instruction).getFile().getSrc();
                int numberOfSources = countSpaces(source);
                LOGGER.debug("Evaluating COPY instruction: '{}' found {} sources", instruction.getText(),
                        numberOfSources);
                consecutiveCopyInstructions += numberOfSources;
            }
            if (consecutiveCopyInstructions >= 7) {
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

}
