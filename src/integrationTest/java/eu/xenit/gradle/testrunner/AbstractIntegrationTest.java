package eu.xenit.gradle.testrunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import eu.xenit.gradle.testrunner.versions.GradleVersionSeries;
import eu.xenit.gradle.testrunner.versions.GradleVersionSpec;
import eu.xenit.gradle.testrunner.versions.VersionFetcher;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.gradle.util.GradleVersion;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Created by thijs on 3/2/17.
 */
@RunWith(Parameterized.class)
public abstract class AbstractIntegrationTest {

    @Parameter(0)
    public String gradleVersion;

    @Parameters(name = "Gradle v{0}")
    public static List<String[]> testData() {
        String forceGradleVersion = System.getProperty("eu.xenit.gradle.integration.useGradleVersion");
        if (forceGradleVersion != null) {
            return Arrays.asList(new String[][]{
                    {forceGradleVersion},
            });
        }

        boolean majorsOnly = Boolean.getBoolean("eu.xenit.gradle.integration.majorsOnly");
        List<String[]> versionsToBuild = VersionFetcher.fetchVersionSeries()
                // Only release versions >= 5.6
                .map(series -> series.filter(VersionFetcher::isRelease)
                        .filter(VersionFetcher.greaterThanOrEqual("5.6")))
                .filter(series -> !series.isEmpty())
                // When selecting only majors, select the series with only the major version component
                .filter(series -> !majorsOnly || series.getSeriesPriority() == 1)
                // For series with only the major version component, select the first and last version, for series with the minor version component, only select the last version
                .flatMap(series -> series.getSeriesPriority() == 1 ? Stream.of(series.getLowestVersion(), series.getHighestVersion()): Stream.of(
                        series.getHighestVersion()))
                // Collect to a set to deduplicate versions
                .collect(Collectors.toSet())
                .stream()
                // Re-sort by version number
                .sorted(GradleVersionSpec.BY_VERSION_NUMBER)
                // Convert to parameter for junit
                .map(GradleVersionSpec::getVersion)
                .map(version -> new String[]{version})
                .collect(Collectors.toList());

        List<String[]> slicedVersions = slice(versionsToBuild);

        System.out.println(slicedVersions.stream()
                .map(i -> i[0])
                .collect(Collectors.joining(", ", "Running test on versions: ", "")));
        return slicedVersions;
    }

    private static <T> List<T> slice(List<T> items) {
        try {
            int sliceTotal = Integer.parseUnsignedInt(
                    System.getProperty("eu.xenit.gradle.integration.slice.total", "1"));
            int sliceIndex = Integer.parseUnsignedInt(
                    System.getProperty("eu.xenit.gradle.integration.slice.index", "0"));
            int itemsPerSlice = (int)Math.ceil(items.size()/(float)sliceTotal);
            System.out.println("Executing slice "+(sliceIndex+1)+"/"+sliceTotal+ " with "+itemsPerSlice+" items per slice");
            return items.subList(itemsPerSlice*sliceIndex, Math.min(itemsPerSlice*(sliceIndex+1), items.size()));
        } catch (NumberFormatException e) {
            return items;
        }
    }


    protected void testProjectFolderThatShouldFail(Path projectFolder, String task) throws IOException {
        testProjectFolder(projectFolder, task, true);
    }

    protected void testProjectFolder(Path projectFolder, String task) throws IOException {
        testProjectFolder(projectFolder, task, false);
    }

    protected void testProjectFolder(Path projectFolder, String task, boolean expectsException) throws IOException {
        if (expectsException) {
            testProjectFolderExpectFailure(projectFolder, task, null);
        } else {
            testProjectFolderExpectSucces(projectFolder, task, null);
        }
    }

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    protected void testProjectFolderExpectSucces(Path projectFolder, String task, String message) throws IOException {

        BuildResult buildResult = getGradleRunner(projectFolder, task).build();
        assertEquals(TaskOutcome.SUCCESS, buildResult.task(task).getOutcome());
        if (message != null) {
            assertTrue(buildResult.getOutput().contains(message));
        }
    }

    protected void testProjectFolderExpectFailure(Path projectFolder, String task, String message) throws IOException {
        BuildResult buildResult = getGradleRunner(projectFolder, task).buildAndFail();
        if (message != null) {
            assertTrue(buildResult.getOutput().contains(message));
        }

    }

    protected GradleRunner getGradleRunner(Path projectFolder, String task) throws IOException {
        return getGradleRunner(projectFolder, task, "--rerun-tasks");
    }

    protected GradleRunner getGradleRunner(Path projectFolder, String task, String... additionalArguments) throws IOException {
        File tempExample = getOrCreateTemporaryFolder(projectFolder.getFileName().toString());
        FileUtils.copyDirectory(projectFolder.toFile(), tempExample);
        File gitDir = tempExample.toPath().resolve("_git").toFile();
        if (gitDir.exists()) {
            FileUtils.moveDirectory(gitDir, tempExample.toPath().resolve(".git").toFile());
        }

        Set<String> arguments = new LinkedHashSet<>();
        arguments.add(task);
        arguments.add("--stacktrace");
        arguments.add("--info");
        arguments.addAll(Arrays.asList(additionalArguments));

        GradleRunner runner = GradleRunner.create()
                .withProjectDir(tempExample)
                .withArguments(new ArrayList<>(arguments))
                .withPluginClasspath()
                .forwardOutput();

        if(System.getProperty("eu.xenit.gradle.integration.useGradleVersion") == null) {
            return runner.withGradleVersion(gradleVersion);
        }

        return runner;
    }

    protected void testProjectFolder(Path projectFolder) throws IOException {
        testProjectFolder(projectFolder, ":buildDockerImage");
    }

    private File getOrCreateTemporaryFolder(final String name) throws IOException {
        Path folder = temporaryFolder.getRoot().toPath().resolve(name);
        if (Files.exists(folder)) {
            return folder.toFile();
        }
        return temporaryFolder.newFolder(name);
    }
}
