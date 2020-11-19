package eu.xenit.gradle.docker.tasks.internal;

import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import com.bmuschko.gradle.docker.tasks.image.Dockerfile.Instruction;
import java.util.List;
import java.util.stream.Collectors;
import org.gradle.api.Action;
import org.gradle.api.internal.project.DefaultProject;
import org.gradle.testfixtures.ProjectBuilder;

abstract public class AbstractDockerFileActionsTest {

    private static List<String> instructionsToString(List<Instruction> instructionList) {
        return instructionList.stream()
                .map(Instruction::getText)
                .collect(Collectors.toList());
    }


    protected static List<Instruction> executeActionOnInstructions(
            Class<? extends Action<? super Dockerfile>> actionClass, Action<Dockerfile> configure) {
        DefaultProject project = (DefaultProject) ProjectBuilder.builder().build();
        Dockerfile dockerfileTask = project.getTasks().register("createDockerFile", Dockerfile.class, configure).get();

        project.getObjects().newInstance(actionClass).execute(dockerfileTask);
        return dockerfileTask.getInstructions().get();
    }

    protected static List<String> executeActionOnInstructionsToString(
            Class<? extends Action<? super Dockerfile>> actionClass, Action<Dockerfile> configure) {
        return instructionsToString(executeActionOnInstructions(actionClass, configure));
    }
}
