package eu.xenit.gradle.docker.alfresco.tasks;


import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TFile;
import eu.xenit.gradle.docker.alfresco.internal.version.AlfrescoVersion;
import eu.xenit.gradle.docker.internal.Deprecation;
import eu.xenit.gradle.docker.tasks.DockerfileWithCopyTask;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.GradleVersion;

public class DockerfileWithWarsTask extends DockerfileWithCopyTask implements LabelConsumerTask {

    public static final String MESSAGE_BASE_IMAGE_NOT_SET = "Base image not set. You need to configure your base image to build docker images.";
    /**
     * Base image used to build the dockerfile
     */
    private Property<String> baseImage = getProject().getObjects().property(String.class);

    /**
     * Map of labels to add to the dockerfile
     */
    private final MapProperty<String, String> labels = getProject().getObjects()
            .mapProperty(String.class, String.class);

    /**
     * Map of directories in the tomcat folder to the war file to place there
     */
    private final Map<String, List<Provider<java.io.File>>> warFiles = new HashMap<>();

    /**
     * Target directory inside the docker container where war files will be placed
     */
    private Property<String> targetDirectory = getProject().getObjects().property(String.class)
            .convention("/usr/local/tomcat/webapps/");

    public DockerfileWithWarsTask() {
        if (GradleVersion.current().compareTo(GradleVersion.version("5.6")) >= 0) {
            from(baseImage.orElse(getProject().provider(() -> {
                throw new IllegalStateException(MESSAGE_BASE_IMAGE_NOT_SET);
            })).map(From::new));
        } else {
            from(baseImage.map(From::new));
        }
        baseImage.set((String) null);
    }

    @Input
    public Property<String> getTargetDirectory() {
        return targetDirectory;
    }


    /**
     * Before adding the new war to the image, remove the expanded folder in the webapps.
     */
    private Property<Boolean> removeExistingWar = getProject().getObjects().property(Boolean.class).convention(true);


    @Input
    public Property<Boolean> getRemoveExistingWar() {
        return removeExistingWar;
    }

    /**
     * Check if Alfresco version that is already present in the container matches the Alfresco version of the war that will be added.
     */
    private Property<Boolean> checkAlfrescoVersion = getProject().getObjects().property(Boolean.class)
            .convention(getRemoveExistingWar().map(b -> !b));

    @Input
    public Property<Boolean> getCheckAlfrescoVersion() {
        return checkAlfrescoVersion;
    }

    /**
     * Adds a prefix to log4j log lines inside an extracted WAR
     *
     * @param destinationDir
     * @param logName
     */
    private void improveLog4j(java.io.File destinationDir, String logName) {
        Path path = destinationDir.toPath().resolve(Paths.get("WEB-INF", "classes", "log4j.properties"));
        if (Files.exists(path)) {
            getLogger().info("Prefixing logs for {} with [{}]", destinationDir.getName(), logName);
            Charset charset = StandardCharsets.UTF_8;
            try {
                String content = new String(Files.readAllBytes(path), charset);
                content = content.replaceAll("log4j\\.rootLogger=error,\\ Console,\\ File",
                        "log4j\\.rootLogger=error,\\ Console");
                //prefix the loglines with the base
                content = content
                        .replaceAll("log4j\\.appender\\.Console\\.layout\\.ConversionPattern=\\%d\\{ISO8601\\}",
                                "log4j\\.appender\\.Console\\.layout\\.ConversionPattern=\\[" + logName
                                        + "\\]\\ %d\\{ISO8601\\}");
                Files.write(path, content.getBytes(charset));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        } else {
            getLogger().info("No log4j.properties available in {}. Not changing the console appender",
                    destinationDir.getName());
        }
    }

    /**
     * Unzips a war file to a directory
     *
     * @param warFile
     * @param destinationDir
     */
    private static void unzipWar(java.io.File warFile, java.io.File destinationDir) {
        if (warFile == null) {
            return;
        }
        Util.withWar(warFile, archive -> {
            TFile directory = new TFile(destinationDir);
            directory.mkdirs();
            try {
                TFile.cp_rp(archive, directory, TArchiveDetector.NULL);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Input
    public Property<String> getBaseImage() {
        return baseImage;
    }

    @InputFiles
    public FileCollection getWarFiles() {
        ConfigurableFileCollection mappedWarFiles = getProject().files(getProject().provider(() -> {
            return warFiles.values().stream()
                    .flatMap(Collection::stream)
                    .filter(Provider::isPresent)
                    .map(Provider::get)
                    .collect(Collectors.toList());
        }));
        // Filter with an always matching filter, so the returned FileCollection is no longer a ConfigurableFileCollection
        return mappedWarFiles.filter(x -> true);
    }

    public void addWar(String name, WarLabelOutputTask task) {
        dependsOn(task);
        addWar(name, task.getOutputWar());
        withLabels(task);
    }

    public void addWar(String name, Provider<RegularFile> regularFileProvider) {
        _addWar(name, regularFileProvider.map(RegularFile::getAsFile));
    }

    public void addWar(String name, java.io.File file) {
        _addWar(name, getProject().provider(() -> file));
    }

    private void _addWar(String name, Provider<java.io.File> file) {
        if (!warFiles.containsKey(name)) {
            warFiles.put(name, new ArrayList<>());
        }
        warFiles.get(name).add(file);
    }

    @Deprecated
    public void addWar(String name, Supplier<java.io.File> file) {
        Deprecation.warnDeprecatedReplaced("addWar(String name, Supplier<File> file)",
                "addWar(String name, Provider<RegularFile> fileProvider)");
        _addWar(name, getProject().provider(file::get));
    }

    public void addWar(String name, Configuration configuration) {
        dependsOn(configuration);
        _addWar(name, getProject().provider(configuration::getSingleFile));
    }

    @TaskAction
    @Override
    public void create() {
        // Unpack & COPY into container
        warFiles.forEach((name, wars) -> {
            java.io.File destinationDir = getDestFile().getAsFile().get().toPath().resolveSibling(name).toFile();
            if (destinationDir.exists()) {
                try {
                    TFile.rm_r(destinationDir);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            wars.forEach(war -> {
                // Unpack war
                unzipWar(war.getOrNull(), destinationDir);
            });

            if (destinationDir.exists()) {
                improveLog4j(destinationDir, name.toUpperCase());

                // COPY
                if (getRemoveExistingWar().get()) {
                    runCommand("rm -rf " + getTargetDirectory().get() + name);
                }
                if (getCheckAlfrescoVersion().get()) {
                    try {
                        AlfrescoVersion alfrescoVersion = AlfrescoVersion.fromAlfrescoWar(destinationDir.toPath());
                        if (alfrescoVersion != null) {
                            this.runCommand(alfrescoVersion.getCheckCommand(getTargetDirectory().get() + name));
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
                copyFile("./" + name, getTargetDirectory().get() + name);
            }
        });

        // LABEL
        if (!getLabels().get().isEmpty()) {
            label(getLabels());
        }

        super.create();
    }

    @Override
    public void withLabels(Provider<Map<String, String>> labels) {
        this.labels.putAll(labels);
    }

    @Input
    public MapProperty<String, String> getLabels() {
        return labels;
    }
}
