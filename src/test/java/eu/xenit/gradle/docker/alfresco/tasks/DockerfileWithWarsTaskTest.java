package eu.xenit.gradle.docker.alfresco.tasks;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import com.bmuschko.gradle.docker.tasks.image.Dockerfile.Instruction;
import eu.xenit.gradle.docker.alfresco.internal.version.AlfrescoVersion;
import eu.xenit.gradle.docker.alfresco.tasks.DockerfileWithWarsTask.ElideDuplicateVersionChecksAction;
import eu.xenit.gradle.docker.alfresco.tasks.DockerfileWithWarsTask.RemoveNoOpInstructionsAction;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class DockerfileWithWarsTaskTest {

    private static DockerfileWithWarsTask createDockerfile(Action<DockerfileWithWarsTask> configure) {
        Project project = ProjectBuilder.builder().build();
        return project.getTasks().register("createDockerFile", DockerfileWithWarsTask.class, configure).get();
    }

    private static List<String> instructionsToString(Dockerfile dockerfile) {
        return instructionsToString(dockerfile.getInstructions().get());
    }

    private static List<String> instructionsToString(List<? extends Instruction> instructionList) {
        return instructionList.stream()
                .map(Instruction::getText)
                .collect(Collectors.toList());
    }

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private File warFile;

    @Before
    public void copyWar() throws IOException {
        warFile = temporaryFolder.newFile();
        warFile.delete();
        Files.copy(getClass().getResourceAsStream("/test123.war"), warFile.toPath());
    }

    @Test
    public void requiresBaseImage() {
        DockerfileWithWarsTask dockerfileWithWarsTask = createDockerfile(dockerfile -> {

        });

        expectedException.expectMessage(is(DockerfileWithWarsTask.MESSAGE_BASE_IMAGE_NOT_SET));
        // This results in an exception when trying to resolve the FROM instruction
        instructionsToString(dockerfileWithWarsTask);
    }

    @Test
    public void defaultAddWarSettings() {
        DockerfileWithWarsTask dockerfileWithWarsTask = createDockerfile(dockerfile -> {
            dockerfile.getBaseImage().set("scratch");
            dockerfile.addWar("abc", warFile);
            dockerfile.addWar("abc", warFile);
            dockerfile.addWar("def", warFile);
        });

        new RemoveNoOpInstructionsAction().execute(dockerfileWithWarsTask);

        List<String> instructions = instructionsToString(dockerfileWithWarsTask);

        assertEquals(Arrays.asList(
                "FROM scratch",
                "RUN rm -rf "+dockerfileWithWarsTask.getTargetDirectory().get()+"abc",
                "COPY copyFile/1/ "+dockerfileWithWarsTask.getTargetDirectory().get()+"abc/",
                "COPY copyFile/2/ "+dockerfileWithWarsTask.getTargetDirectory().get()+"abc/",
                "RUN rm -rf "+dockerfileWithWarsTask.getTargetDirectory().get()+"def",
                "COPY copyFile/3/ "+dockerfileWithWarsTask.getTargetDirectory().get()+"def/"
        ), instructions);
    }

    @Test
    public void removeNoOpInstructions() {
        DockerfileWithWarsTask dockerfileWithWarsTask = createDockerfile(dockerfile -> {
            dockerfile.getBaseImage().set("scratch");
            dockerfile.getRemoveExistingWar().set(false);
            dockerfile.addWar("abc", warFile);
        });


        List<String> instructionsBeforeRemove = instructionsToString(dockerfileWithWarsTask);

        assertTrue("Contains a no-op RUN instruction",
                instructionsBeforeRemove.stream().anyMatch(i -> i.startsWith("RUN true")));

        new RemoveNoOpInstructionsAction().execute(dockerfileWithWarsTask);

        List<String> instructionsAfterRemove = instructionsToString(dockerfileWithWarsTask);

        assertFalse("Contains a no-op RUN instruction",
                instructionsAfterRemove.stream().anyMatch(i -> i.startsWith("RUN true")));
    }

    @Test
    public void elideDuplicateVersionCheckInstructions() throws IOException {

        DockerfileWithWarsTask dockerfileWithWarsTask = createDockerfile(dockerfile -> {
            dockerfile.getBaseImage().set("scratch");
            dockerfile.getRemoveExistingWar().set(false);
            dockerfile.addWar("abc", warFile);
            dockerfile.addWar("abc", warFile);
            dockerfile.addWar("def", warFile);
        });

        String versionCheckCommand = Objects.requireNonNull(AlfrescoVersion.fromAlfrescoWar(warFile.toPath()))
                .getCheckCommand(dockerfileWithWarsTask.getTargetDirectory().get() + "abc");

        List<String> instructionsBeforeRemove = instructionsToString(dockerfileWithWarsTask);

        assertEquals(2,
                instructionsBeforeRemove.stream().filter(i -> i.startsWith("RUN " + versionCheckCommand)).count());

        new ElideDuplicateVersionChecksAction().execute(dockerfileWithWarsTask);

        List<String> instructionsAfterRemove = instructionsToString(dockerfileWithWarsTask);
        assertEquals(1,
                instructionsAfterRemove.stream().filter(i -> i.startsWith("RUN " + versionCheckCommand)).count());

        // Check that check command for other path is not elided
        String otherVersionCheckCommand = Objects.requireNonNull(AlfrescoVersion.fromAlfrescoWar(warFile.toPath()))
                .getCheckCommand(dockerfileWithWarsTask.getTargetDirectory().get() + "def");
        assertEquals(1,
                instructionsAfterRemove.stream().filter(i -> i.startsWith("RUN " + otherVersionCheckCommand)).count());
    }

    @Test
    public void lazyResolveRemoveExistingWar() {
        DockerfileWithWarsTask dockerfileWithWarsTask = createDockerfile(dockerfile -> {
            dockerfile.getBaseImage().set("scratch");
            dockerfile.addWar("abc", warFile);
            dockerfile.addWar("abc", warFile);
            dockerfile.addWar("def", warFile);
            dockerfile.getRemoveExistingWar().set(false);
        });

        List<String> instructions = instructionsToString(dockerfileWithWarsTask);

        assertEquals("Contains no rm -rf of abc war", 0, instructions.stream()
                .filter(i -> i.startsWith("RUN rm -rf " + dockerfileWithWarsTask.getTargetDirectory().get() + "abc"))
                .count());
    }

    @Test
    public void lazyResolveTargetDirectory() {
        DockerfileWithWarsTask dockerfileWithWarsTask = createDockerfile(dockerfile -> {
            dockerfile.getBaseImage().set("scratch");
            dockerfile.addWar("abc", warFile);
            dockerfile.addWar("abc", warFile);
            dockerfile.addWar("def", warFile);
            dockerfile.getTargetDirectory().set("/some/other/path");
        });

        List<String> instructions = instructionsToString(dockerfileWithWarsTask);
        assertTrue("Target directory has changed", instructions.stream().anyMatch(i -> i.contains("/some/other/path")));
    }

    @Test
    public void lazyResolveCheckAlfrescoVersion() throws IOException {
        DockerfileWithWarsTask dockerfileWithWarsTask = createDockerfile(dockerfile -> {
            dockerfile.getBaseImage().set("scratch");
            dockerfile.getRemoveExistingWar().set(false);
            dockerfile.addWar("abc", warFile);
            dockerfile.addWar("abc", warFile);
            dockerfile.addWar("def", warFile);
            dockerfile.getCheckAlfrescoVersion().set(false);
        });

        List<String> instructions = instructionsToString(dockerfileWithWarsTask);
        String versionCheckCommand = Objects.requireNonNull(AlfrescoVersion.fromAlfrescoWar(warFile.toPath()))
                .getCheckCommand(dockerfileWithWarsTask.getTargetDirectory().get() + "abc");
        assertTrue("No alfresco version checks are in the instructions", instructions.stream().noneMatch(i -> i.startsWith("RUN "+versionCheckCommand)));
    }
}
