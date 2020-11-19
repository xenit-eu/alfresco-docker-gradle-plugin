package eu.xenit.gradle.docker.tasks.internal;

import static org.junit.Assert.assertEquals;

import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import java.util.Arrays;
import java.util.List;
import org.gradle.api.Action;
import org.junit.Test;

public class Workaround7ConsecutiveCopyDockerBugActionTest extends AbstractDockerFileActionsTest {

    protected static List<String> createWorkaroundInstructionsString(Action<Dockerfile> configure) {
        return executeActionOnInstructionsToString(Workaround7ConsecutiveCopyDockerBugAction.class, configure);
    }

    @Test
    public void testInsertCommandAfterCopy() {
        List<String> instructions = createWorkaroundInstructionsString(dockerfile -> {
            for (int i = 0; i < 10; i++) {
                dockerfile.copyFile("dir" + i + "/", "target/");
            }
        });

        assertEquals(Arrays.asList(
                "COPY dir0/ target/",
                "COPY dir1/ target/",
                "COPY dir2/ target/",
                "COPY dir3/ target/",
                "COPY dir4/ target/",
                "COPY dir5/ target/",
                "RUN true",
                "COPY dir6/ target/",
                "COPY dir7/ target/",
                "COPY dir8/ target/",
                "COPY dir9/ target/"
        ), instructions);
    }

    @Test
    public void testInsertCommandAfterMultiCopy() {

        List<String> instructions = createWorkaroundInstructionsString(dockerfile -> {
            for (int i = 0; i < 10; i += 2) {
                dockerfile.copyFile("dir" + i + "/ dir" + (i + 1) + "/", "target/");
            }
        });

        assertEquals(Arrays.asList(
                "COPY dir0/ dir1/ target/",
                "COPY dir2/ dir3/ target/",
                "COPY dir4/ dir5/ target/",
                "RUN true",
                "COPY dir6/ dir7/ target/",
                "COPY dir8/ dir9/ target/"
        ), instructions);
    }

    @Test
    public void testDoNotInsertCommandWhenNotTooManyCopy() {

        List<String> instructions = createWorkaroundInstructionsString(dockerfile -> {
            dockerfile.copyFile("dir1/ dir2/ dir3/ dir4/ dir5/ dir6/", "target/");
            dockerfile.runCommand("some-command");
            dockerfile.copyFile("dir7/ dir8/ dir9/", "target/");
        });

        assertEquals(Arrays.asList(
                "COPY dir1/ dir2/ dir3/ dir4/ dir5/ dir6/ target/",
                "RUN some-command",
                "COPY dir7/ dir8/ dir9/ target/"
        ), instructions);
    }

    @Test
    public void testSplitsUpTooLargeConsolidatedCopy() {
        List<String> instructions = createWorkaroundInstructionsString(dockerfile -> {
            dockerfile.copyFile("dir1/ dir2/ dir3/ dir4/ dir5/ dir6/ dir7/ dir8/", "target/");
        });

        assertEquals(Arrays.asList(
                "COPY dir1/ dir2/ dir3/ dir4/ dir5/ dir6/ target/",
                "RUN true",
                "COPY dir7/ dir8/ target/"
        ), instructions);
    }
}
