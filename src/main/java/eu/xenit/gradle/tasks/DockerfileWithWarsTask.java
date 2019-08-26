package eu.xenit.gradle.tasks;

import static eu.xenit.gradle.tasks.VersionMatchChecking.getCanAddWarsCheckCommands;

import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TFile;
import eu.xenit.gradle.docker.internal.Deprecation;
import groovy.lang.Closure;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;

public class DockerfileWithWarsTask extends Dockerfile implements LabelConsumerTask {

    /**
     * Base image used to build the dockerfile
     */
    private Property<String> baseImage = getProject().getObjects().property(String.class);

    /**
     * Map of labels to add to the dockerfile
     */
    private final List<Supplier<Map<String, String>>> labels = new ArrayList<>();

    /**
     * Map of directories in the tomcat folder to the war file to place there
     */
    private final Map<String, List<Supplier<java.io.File>>> warFiles = new HashMap<>();

    /**
     * Target directory inside the docker container where war files will be placed
     */
    private String targetDirectory = "/usr/local/tomcat/webapps/";

    @Input
    public String getTargetDirectory() {
        return targetDirectory;
    }

    public DockerfileWithWarsTask setTargetDirectory(String targetDirectory) {
        this.targetDirectory = targetDirectory;
        return this;
    }


    /**
     * Before adding the new war to the image, remove the expanded folder in the webapps.
     */
    private boolean removeExistingWar = true;


    @Input
    public boolean getRemoveExistingWar() {
        return removeExistingWar;
    }

    public void setRemoveExistingWar(boolean removeExistingWar) {
        this.removeExistingWar = removeExistingWar;
    }

    /**
     * Check if Alfresco version that is already present in the container matches the Alfresco version of the war that will be added.
     */
    private BooleanSupplier checkAlfrescoVersion = () -> !this.getRemoveExistingWar();

    @Input
    public boolean getCheckAlfrescoVersion() {
        return checkAlfrescoVersion.getAsBoolean();
    }

    public void setCheckAlfrescoVersion(boolean checkAlfrescoVersion) {
        this.checkAlfrescoVersion = () -> checkAlfrescoVersion;
    }

    public void setCheckAlfrescoVersion(BooleanSupplier checkAlfrescoVersion) {
        this.checkAlfrescoVersion = checkAlfrescoVersion;
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
            getLogger().info("Prefixing logs for " + destinationDir.getName() + " with [" + logName + "]");
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
                e.printStackTrace();
                throw new IllegalStateException(e);
            }
        } else {
            getLogger().info("No log4j.properties available in " + destinationDir.getName()
                    + ". Not changing the console appender");
        }
    }

    /**
     * Unzips a war file to a directory
     *
     * @param warFile
     * @param destinationDir
     */
    private static void unzipWar(java.io.File warFile, java.io.File destinationDir) {
        if(warFile == null) {
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
    public String getBaseImage() {
        return baseImage.get();
    }

    public void setBaseImage(String baseImage) {
        setBaseImage(getProject().provider(() -> baseImage));
    }

    public void setBaseImage(Provider<String> baseImage) {
        if (this.baseImage.isPresent()) {
            throw new IllegalStateException("Base image can only be set once.");
        }
        this.baseImage.set(baseImage);
    }

    /**
     * @deprecated since 4.1.0, will be removed in 5.0.0
     */
    @Deprecated
    public void setBaseImage(Supplier<String> baseImage) {
        Deprecation.warnDeprecatedReplacedBy("setBaseImage(Provider<String>)");
        setBaseImage(getProject().provider(baseImage::get));
    }

    /**
     * @deprecated since 4.1.0, will be removed in 5.0.0
     */
    @Deprecated
    public void setBaseImage(Closure<String> baseImage) {
        Deprecation.warnDeprecatedReplacedBy("setBaseImage(Provider<String>)");
        setBaseImage(getProject().provider(baseImage::call));
    }


    /**
     * @deprecated in 3.x, will be removed in 5.0.0
     */
    @Deprecated
    public void setAlfrescoWar(java.io.File alfrescoWar) {
        Deprecation.warnDeprecatedReplacedBy("addWar(\"alfresco\", alfrescoWar)");
        addWar("alfresco", alfrescoWar);
    }

    /**
     * @deprecated in 3.x, will be removed in 5.0.0
     */
    @Deprecated
    public void setShareWar(java.io.File shareWar) {
        Deprecation.warnDeprecatedReplacedBy("addWar(\"alfresco\", alfrescoWar)");
        addWar("share", shareWar);
    }

    @InputFiles
    public Collection<java.io.File> getWarFiles() {
        return warFiles.values().stream()
                .flatMap(Collection::stream)
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void addWar(String name, WarLabelOutputTask task) {
        dependsOn(task);
        addWar(name, task::getOutputWar);
        withLabels(task);
    }

    public void addWar(String name, java.io.File file) {
        addWar(name, () -> file);
    }

    public void addWar(String name, Supplier<java.io.File> file) {
        if (!warFiles.containsKey(name)) {
            warFiles.put(name, new LinkedList<>());
        }
        warFiles.get(name).add(file);
    }

    public void addWar(String name, Configuration configuration) {
        dependsOn(configuration);
        addWar(name, configuration::getSingleFile);
    }

    @TaskAction
    @Override
    public void create() {
        from(baseImage.map(From::new));
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
            wars.forEach((war) -> {
                // Unpack war
                unzipWar(war.get(), destinationDir);
            });

            if(destinationDir.exists()) {
            improveLog4j(destinationDir, name.toUpperCase());

            // COPY
            if (getRemoveExistingWar()) {
                runCommand("rm -rf " + getTargetDirectory() + name);
            }
            if (getCheckAlfrescoVersion()) {
                getCanAddWarsCheckCommands(destinationDir, getTargetDirectory()).forEach(this::runCommand);
            }
            DockerfileWithWarsTask.this.copyFile("./" + name, getTargetDirectory() + name);
            }
        });

        // LABEL
        Map<String, String> labels = getLabels();
        if (!labels.isEmpty()) {
            label(labels);
        }

        super.create();
    }

    @Override
    public void withLabels(Supplier<Map<String, String>> labels) {
        this.labels.add(labels);
    }

    @Input
    public Map<String, String> getLabels() {
        Map<String, String> accumulator = new HashMap<>();
        for (Supplier<Map<String, String>> supplier : labels) {
            accumulator.putAll(supplier.get());
        }
        return accumulator;
    }
}
