package eu.xenit.gradle.tasks;

import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TFile;
import groovy.lang.Closure;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static eu.xenit.gradle.tasks.VersionMatchChecking.getCanAddWarsCheckCommands;

public class DockerfileWithWarsTask extends Dockerfile implements LabelConsumerTask {
    /**
     * Base image used to build the dockerfile
     */
    private Supplier<String> baseImage;

    /**
     * Map of labels to add to the dockerfile
     */
    private List<Supplier<Map<String, String>>> labels = new ArrayList<>();

    /**
     * Map of directories in the tomcat folder to the war file to place there
     */
    private Map<String, List<Supplier<File>>> warFiles = new HashMap<>();

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
     * Adds a prefix to log4j log lines inside an extracted WAR
     *
     * @param destinationDir
     * @param logName
     */
    private void improveLog4j(File destinationDir, String logName) {
        Path path = destinationDir.toPath().resolve(Paths.get("WEB-INF", "classes", "log4j.properties"));
        if (Files.exists(path)) {
            getLogger().info("Prefixing logs for " + destinationDir.getName() + " with [" + logName + "]");
            Charset charset = StandardCharsets.UTF_8;
            try {
                String content = new String(Files.readAllBytes(path), charset);
                content = content.replaceAll("log4j\\.rootLogger=error,\\ Console,\\ File", "log4j\\.rootLogger=error,\\ Console");
                //prefix the loglines with the base
                content = content.replaceAll("log4j\\.appender\\.Console\\.layout\\.ConversionPattern=\\%d\\{ISO8601\\}", "log4j\\.appender\\.Console\\.layout\\.ConversionPattern=\\[" + logName + "\\]\\ %d\\{ISO8601\\}");
                Files.write(path, content.getBytes(charset));
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalStateException(e);
            }
        } else {
            getLogger().info("No log4j.properties available in " + destinationDir.getName() + ". Not changing the console appender");
        }
    }

    /**
     * Unzips a war file to a directory
     *
     * @param warFile
     * @param destinationDir
     */
    private static void unzipWar(File warFile, File destinationDir) {
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
        if (this.baseImage != null)
            throw new IllegalStateException("Base image can only be set once.");
        this.baseImage = () -> baseImage;
        super.from(baseImage);
    }

    public void setBaseImage(Supplier<String> baseImage) {
        if (this.baseImage != null)
            throw new IllegalStateException("Base image can only be set once.");
        this.baseImage = baseImage;
        super.from(new Closure<String>(this) {
            @Override
            public String call() {
                return baseImage.get();
            }
        });
    }

    public void setBaseImage(Closure<String> baseImage) {
        setBaseImage(() -> baseImage.call());
    }

    @Override
    public void from(String image) {
        throw new UnsupportedOperationException("Do not use FROM directly, use setBaseImage()");
    }


    @Deprecated
    public void setAlfrescoWar(File alfrescoWar) {
        getLogger().warn("setAlfrescoWar(alfrescoWar) is deprecated and will be removed in xenit-gradle-plugins 4.0. Use addWar(\"alfresco\", alfrescoWar) instead.");
        addWar("alfresco", alfrescoWar);
    }

    @Deprecated
    public void setShareWar(File shareWar) {
        getLogger().warn("setShareWar(shareWar) is deprecated and will be removed in xenit-gradle-plugins 4.0. Use addWar(\"share\", shareWar) instead.");
        addWar("share", shareWar);
    }

    @InputFiles
    public Collection<File> getWarFiles() {
        return warFiles.values().stream()
                .flatMap(Collection::stream)
                .map(Supplier::get)
                .collect(Collectors.toList());
    }

    public void addWar(String name, WarLabelOutputTask task) {
        dependsOn(task);
        addWar(name, task::getOutputWar);
        withLabels(task);
    }

    public void addWar(String name, File file) {
        addWar(name, () -> file);
    }

    public void addWar(String name, Supplier<File> file) {
        if (!warFiles.containsKey(name)) {
            warFiles.put(name, new LinkedList<>());
        }
        warFiles.get(name).add(file);
    }

    public void addWar(String name, Configuration configuration) {
        dependsOn(configuration);
        addWar(name, configuration::getSingleFile);
    }

    @Input
    public boolean isLeanImage() {
        if (getLeanImageSupplier != null) {
            return this.getLeanImageSupplier.get();
        }
        return false;
    }

    private Supplier<Boolean> getLeanImageSupplier;

    public void setLeanImageSupplier(Supplier<Boolean> getLeanImageSupplier) {
        this.getLeanImageSupplier = getLeanImageSupplier;
    }

    @TaskAction
    @Override
    public void create() {
        // Unpack & COPY into container
        warFiles.forEach((name, wars) -> {
            File destinationDir = DockerfileWithWarsTask.this.getDestFile().toPath().resolveSibling(name).toFile();
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
            improveLog4j(destinationDir, name.toUpperCase());

            // COPY
            if (removeExistingWar) {
                DockerfileWithWarsTask.this.runCommand("rm -rf " + getTargetDirectory() + name);
            }
            if (isLeanImage()) {
                getCanAddWarsCheckCommands(destinationDir,getTargetDirectory()).forEach(e -> runCommand(e));
            }
            DockerfileWithWarsTask.this.copyFile("./" + name, getTargetDirectory() + name);
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
