package eu.xenit.gradle.docker.alfresco.tasks;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import com.bmuschko.gradle.docker.tasks.image.Dockerfile.Instruction;
import eu.xenit.gradle.docker.alfresco.internal.version.AlfrescoVersion;
import eu.xenit.gradle.docker.alfresco.tasks.extension.internal.DockerfileWithWarsExtensionImpl;
import eu.xenit.gradle.docker.alfresco.tasks.extension.internal.DockerfileWithWarsExtensionImpl.ElideDuplicateVersionChecksAction;
import eu.xenit.gradle.docker.alfresco.tasks.extension.internal.DockerfileWithWarsExtensionImpl.RemoveNoOpInstructionsAction;
import eu.xenit.gradle.docker.alfresco.tasks.extension.DockerfileWithWarsExtension;
import eu.xenit.gradle.docker.alfresco.tasks.extension.internal.DockerfileWithWarsExtensionImpl.ValidateBaseImageSet;
import eu.xenit.gradle.docker.tasks.DockerfileWithCopyTask;
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

public class DockerfileWithWarsExtensionTest {

    private static DockerfileWithCopyTask createDockerfile(Action<DockerfileWithCopyTask> configure) {
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
        DockerfileWithCopyTask dockerfileWithWarsTask = createDockerfile(dockerfile -> {

        });

        expectedException.expectMessage(is(DockerfileWithWarsExtensionImpl.MESSAGE_BASE_IMAGE_NOT_SET));

        new DockerfileWithWarsExtensionImpl.ValidateBaseImageSet().execute(dockerfileWithWarsTask);
    }

    @Test
    public void defaultAddWarSettings() {
        DockerfileWithCopyTask dockerfileWithWarsTask = createDockerfile(dockerfile -> {
            DockerfileWithWarsExtension convention = DockerfileWithWarsExtension.get(dockerfile);
            convention.getBaseImage().set("scratch");
            convention.addWar("abc", warFile);
            convention.addWar("abc", warFile);
            convention.addWar("def", warFile);
        });

        new RemoveNoOpInstructionsAction().execute(dockerfileWithWarsTask);

        List<String> instructions = instructionsToString(dockerfileWithWarsTask);

        DockerfileWithWarsExtension convention = DockerfileWithWarsExtension.get(dockerfileWithWarsTask);

        assertEquals(Arrays.asList(
                "FROM scratch",
                "RUN rm -rf " + convention.getTargetDirectory().get() + "abc",
                "COPY copyFile/1/ " + convention.getTargetDirectory().get() + "abc/",
                "COPY copyFile/2/ " + convention.getTargetDirectory().get() + "abc/",
                "RUN rm -rf " + convention.getTargetDirectory().get() + "def",
                "COPY copyFile/3/ " + convention.getTargetDirectory().get() + "def/"
        ), instructions);
    }

    @Test
    public void removeNoOpInstructions() {
        Dockerfile dockerfileWithWarsTask = createDockerfile(dockerfile -> {
            DockerfileWithWarsExtension convention = DockerfileWithWarsExtension.get(dockerfile);
            convention.getBaseImage().set("scratch");
            convention.getRemoveExistingWar().set(false);
            convention.addWar("abc", warFile);
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

        Dockerfile dockerfileWithWarsTask = createDockerfile(dockerfile -> {
            DockerfileWithWarsExtension convention = DockerfileWithWarsExtension.get(dockerfile);
            convention.getBaseImage().set("scratch");
            convention.getRemoveExistingWar().set(false);
            convention.addWar("abc", warFile);
            convention.addWar("abc", warFile);
            convention.addWar("def", warFile);
        });

        String versionCheckCommand = Objects.requireNonNull(AlfrescoVersion.fromAlfrescoWar(warFile.toPath()))
                .getCheckCommand(
                        DockerfileWithWarsExtension.get(dockerfileWithWarsTask).getTargetDirectory().get()
                                + "abc");

        List<String> instructionsBeforeRemove = instructionsToString(dockerfileWithWarsTask);

        assertEquals(2,
                instructionsBeforeRemove.stream().filter(i -> i.startsWith("RUN " + versionCheckCommand)).count());

        new ElideDuplicateVersionChecksAction().execute(dockerfileWithWarsTask);

        List<String> instructionsAfterRemove = instructionsToString(dockerfileWithWarsTask);
        assertEquals(1,
                instructionsAfterRemove.stream().filter(i -> i.startsWith("RUN " + versionCheckCommand)).count());

        // Check that check command for other path is not elided
        String otherVersionCheckCommand = Objects.requireNonNull(AlfrescoVersion.fromAlfrescoWar(warFile.toPath()))
                .getCheckCommand(
                        DockerfileWithWarsExtension.get(dockerfileWithWarsTask).getTargetDirectory().get()
                                + "def");
        assertEquals(1,
                instructionsAfterRemove.stream().filter(i -> i.startsWith("RUN " + otherVersionCheckCommand)).count());
    }

    @Test
    public void lazyResolveRemoveExistingWar() {
        Dockerfile dockerfileWithWarsTask = createDockerfile(dockerfile -> {
            DockerfileWithWarsExtension convention = DockerfileWithWarsExtension.get(dockerfile);
            convention.getBaseImage().set("scratch");
            convention.addWar("abc", warFile);
            convention.addWar("abc", warFile);
            convention.addWar("def", warFile);
            convention.getRemoveExistingWar().set(false);
        });

        List<String> instructions = instructionsToString(dockerfileWithWarsTask);

        assertEquals("Contains no rm -rf of abc war", 0, instructions.stream()
                .filter(i -> i.startsWith(
                        "RUN rm -rf " + DockerfileWithWarsExtension.get(dockerfileWithWarsTask)
                                .getTargetDirectory().get() + "abc"))
                .count());
    }

    @Test
    public void lazyResolveTargetDirectory() {
        Dockerfile dockerfileWithWarsTask = createDockerfile(dockerfile -> {
            DockerfileWithWarsExtension convention = DockerfileWithWarsExtension.get(dockerfile);
            convention.getBaseImage().set("scratch");
            convention.addWar("abc", warFile);
            convention.addWar("abc", warFile);
            convention.addWar("def", warFile);
            convention.getTargetDirectory().set("/some/other/path");
        });

        List<String> instructions = instructionsToString(dockerfileWithWarsTask);
        assertTrue("Target directory has changed", instructions.stream().anyMatch(i -> i.contains("/some/other/path")));
    }

    @Test
    public void lazyResolveCheckAlfrescoVersion() throws IOException {
        Dockerfile dockerfileWithWarsTask = createDockerfile(dockerfile -> {
            DockerfileWithWarsExtension convention = DockerfileWithWarsExtension.get(dockerfile);
            convention.getBaseImage().set("scratch");
            convention.getRemoveExistingWar().set(false);
            convention.addWar("abc", warFile);
            convention.addWar("abc", warFile);
            convention.addWar("def", warFile);
            convention.getCheckAlfrescoVersion().set(false);
        });

        List<String> instructions = instructionsToString(dockerfileWithWarsTask);
        String versionCheckCommand = Objects.requireNonNull(AlfrescoVersion.fromAlfrescoWar(warFile.toPath()))
                .getCheckCommand(
                        DockerfileWithWarsExtension.get(dockerfileWithWarsTask).getTargetDirectory().get()
                                + "abc");
        assertTrue("No alfresco version checks are in the instructions",
                instructions.stream().noneMatch(i -> i.startsWith("RUN " + versionCheckCommand)));
    }
}
