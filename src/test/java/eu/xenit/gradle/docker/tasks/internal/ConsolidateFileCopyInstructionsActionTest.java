package eu.xenit.gradle.docker.tasks.internal;

import static org.junit.Assert.*;

import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import com.bmuschko.gradle.docker.tasks.image.Dockerfile.CopyFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.gradle.api.Action;
import org.junit.Test;

public class ConsolidateFileCopyInstructionsActionTest extends AbstractDockerFileActionsTest {

    protected static List<String> createConsolidatedInstructionsString(Action<Dockerfile> configure) {
        return executeActionOnInstructionsToString(ConsolidateFileCopyInstructionsAction.class, configure);
    }

    @Test
    public void consolidateDirectoriesToSameDirectory() {
        List<String> instructions = createConsolidatedInstructionsString(dockerfile -> {
            dockerfile.copyFile("dir1/", "target/");
            dockerfile.copyFile("dir2/", "target/");
        });

        assertEquals(Collections.singletonList("COPY dir1/ dir2/ target/"), instructions);
    }

    @Test
    public void consolidateFilesToSameDirectory() {
        List<String> instructions = createConsolidatedInstructionsString(dockerfile -> {
            dockerfile.copyFile("file1", "target/");
            dockerfile.copyFile("file2", "target/");
        });

        assertEquals(Collections.singletonList("COPY file1 file2 target/"), instructions);
    }

    @Test
    public void consolidateDirectoriesWithStage() {

        List<String> instructions = createConsolidatedInstructionsString(dockerfile -> {
            dockerfile.copyFile(new CopyFile("dir1/", "target/").withStage("abc"));
            dockerfile.copyFile(new CopyFile("dir2/", "target/").withStage("abc"));
        });

        assertEquals(Collections.singletonList("COPY --from=abc dir1/ dir2/ target/"), instructions);
    }

    @Test
    public void consolidateDirectoriesWithChown() {

        List<String> instructions = createConsolidatedInstructionsString(dockerfile -> {
            dockerfile.copyFile((CopyFile) new CopyFile("dir1/", "target/").withChown("abc"));
            dockerfile.copyFile((CopyFile) new CopyFile("dir2/", "target/").withChown("abc"));
        });

        assertEquals(Collections.singletonList("COPY --chown=abc dir1/ dir2/ target/"), instructions);
    }

    @Test
    public void consolidateFilesToDifferentDirectory() {
        List<String> instructions = createConsolidatedInstructionsString(dockerfile -> {
            dockerfile.copyFile("file1", "target1/");
            dockerfile.copyFile("file2", "target2/");
        });

        assertEquals(Arrays.asList("COPY file1 target1/", "COPY file2 target2/"), instructions);
    }

    @Test
    public void consolidateFilesToSameFile() {
        List<String> instructions = createConsolidatedInstructionsString(dockerfile -> {
            dockerfile.copyFile("file1", "target");
            dockerfile.copyFile("file2", "target");
        });

        assertEquals(Arrays.asList("COPY file1 target", "COPY file2 target"), instructions);
    }

    @Test
    public void consolidateFilesWithDifferentChown() {
        List<String> instructions = createConsolidatedInstructionsString(dockerfile -> {
            dockerfile.copyFile((CopyFile) new CopyFile("file1", "target/").withChown("abc"));
            dockerfile.copyFile((CopyFile) new CopyFile("file2", "target/").withChown("def"));
        });

        assertEquals(Arrays.asList("COPY --chown=abc file1 target/", "COPY --chown=def file2 target/"), instructions);
    }

    @Test
    public void consolidateFilesWithDifferentStage() {
        List<String> instructions = createConsolidatedInstructionsString(dockerfile -> {
            dockerfile.copyFile(new CopyFile("file1", "target/").withStage("abc"));
            dockerfile.copyFile(new CopyFile("file2", "target/").withStage("def"));
        });

        assertEquals(Arrays.asList("COPY --from=abc file1 target/", "COPY --from=def file2 target/"), instructions);
    }

    @Test
    public void consolidateFilesWithInterleavedCommands() {
        List<String> instructions = createConsolidatedInstructionsString(dockerfile -> {
            dockerfile.copyFile(new CopyFile("file1", "target/"));
            dockerfile.runCommand("true");
            dockerfile.copyFile(new CopyFile("file2", "target/"));
        });

        assertEquals(Arrays.asList("COPY file1 target/", "RUN true", "COPY file2 target/"), instructions);
    }

    @Test
    public void consolidateFilesWithDifferentTargets() {
        List<String> instructions = createConsolidatedInstructionsString(dockerfile -> {
            dockerfile.copyFile("dir1/", "target/");
            dockerfile.copyFile("dir2/", "target/");
            dockerfile.copyFile("dir3/", "target2/");
            dockerfile.copyFile("dir4/", "target2/");
        });

        assertEquals(Arrays.asList("COPY dir1/ dir2/ target/", "COPY dir3/ dir4/ target2/"), instructions);
    }
}
